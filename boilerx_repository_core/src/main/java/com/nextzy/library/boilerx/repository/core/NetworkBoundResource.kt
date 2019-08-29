package com.nextzy.library.boilerx.repository.core

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.nextzy.library.boilerx.network.core.ApiEmptyResponse
import com.nextzy.library.boilerx.network.core.ApiErrorResponse
import com.nextzy.library.boilerx.network.core.ApiSuccessResponse
import com.nextzy.library.boilerx.network.core.RequestContainer
import com.nextzy.library.boilerx.repository.core.interceptor.ErrorResponseInterceptor
import com.nextzy.library.boilerx.repository.core.interceptor.RetryInterceptor
import com.nextzy.library.boilerx.repository.core.livedata.RepositoryLiveData
import com.nextzy.library.boilerx.repository.core.vo.Result

@Deprecated(
    message = "This class was deprecated. Please use NetworkBoundResource2"
)
abstract class NetworkBoundResource<InputType, ResultType, RequestType>
@MainThread constructor(
    private val appExecutors: AppExecutors,
    private val errorInterceptor: ErrorResponseInterceptor<InputType, RequestType>? = null,
    private val retryInterceptor: RetryInterceptor? = null,
    private val maxRetry: Int = 1
) {
    private val result = MediatorLiveData<Result<ResultType>>()

    init {
        result.value = Result.Loading
        @Suppress("LeakingThis")
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            appExecutors.diskIO().execute {
                val shouldFetch = shouldFetch(data)
                appExecutors.mainThread().execute {
                    if (shouldFetch) {
                        fetchFromNetwork(dbSource)

                    } else {
                        result.addSource(dbSource) { newData ->
                            setValue(Result.Success(newData))
                        }
                    }
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Result<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue

        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val requestContainer: RequestContainer<InputType, RequestType> = createCall()
        val apiResponse = RepositoryLiveData(
            requestContainer.job,
            retryInterceptor,
            requestContainer.data,
            maxRetry
        )
        apiResponse.execute()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly

        result.addSource(dbSource) {
            setValue(Result.Loading)
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        val data: ResultType = convertToResultType(processResponse(response))
                        saveCallResult(data)
                        appExecutors.mainThread().execute {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Result.Success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Result.Success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    errorInterceptor?.onResponseFailure(requestContainer, response.errorMessage)
                    appExecutors.diskIO().execute {
                        callFailed(response.errorMessage)
                        appExecutors.mainThread().execute {
                            setValue(Result.Error(Exception(response.errorMessage)))
                        }
                    }
                }
            }
        }
    }

    fun asLiveData() = result as LiveData<Result<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body

    @WorkerThread
    protected abstract fun saveCallResult(item: ResultType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): RequestContainer<InputType, RequestType>

    @WorkerThread
    protected abstract fun convertToResultType(response: RequestType): ResultType

    @WorkerThread
    protected abstract fun callFailed(errorMessage: String)
}
