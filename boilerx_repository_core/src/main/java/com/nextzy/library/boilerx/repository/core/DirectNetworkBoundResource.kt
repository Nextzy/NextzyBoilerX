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

abstract class DirectNetworkBoundResource<InputType, ResultType, RequestType>
@MainThread constructor(
        private val appExecutors: AppExecutors,
        private val errorInterceptor: ErrorResponseInterceptor<InputType, RequestType>? = null,
        private val retryInterceptor: RetryInterceptor? = null,
        private val maxRetry: Int = 1
) {
    private val result = MediatorLiveData<Result<ResultType>>()

    init {
        result.value = Result.Loading
        fetchFromNetwork()
    }

    @MainThread
    private fun setValue(newValue: Result<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromNetwork() {
        val requestContainer: RequestContainer<InputType, RequestType> = createCall()
        val apiResponse = RepositoryLiveData(
                requestContainer.job,
                retryInterceptor,
                requestContainer.data,
                maxRetry
        )
        apiResponse.execute()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        setValue(Result.Loading)
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        val data: ResultType = convertToResultType(processResponse(response))
                        appExecutors.mainThread().execute {
                            setValue(Result.Success(data))
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // Empty response so return null directly
                        setValue(Result.Success(null))
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

    @MainThread
    protected abstract fun createCall(): RequestContainer<InputType, RequestType>

    @WorkerThread
    protected abstract fun convertToResultType(response: RequestType): ResultType

    @WorkerThread
    protected abstract fun callFailed(errorMessage: String)
}
