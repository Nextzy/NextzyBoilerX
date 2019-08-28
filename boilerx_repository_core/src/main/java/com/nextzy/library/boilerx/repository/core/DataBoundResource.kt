package com.nextzy.library.boilerx.repository.core

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.nextzy.library.boilerx.network.core.ApiSuccessResponse
import com.nextzy.library.boilerx.repository.core.vo.Result
import kotlinx.coroutines.*
import retrofit2.Response
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class DataBoundResource2<ResultType, ResponseType> {
    protected val result = MediatorLiveData<Result<ResultType>>()

    init {
        launchMainThread {
            result.value = Result.Loading
            @Suppress("LeakingThis")
            val dbSource = loadFromDb()
            result.addSource(dbSource) { data ->
                result.removeSource(dbSource)
                launchWorkerThread {
                    val shouldFetch = shouldFetch(data)
                    mainThread {
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
    }

    @MainThread
    protected fun setValue(newValue: Result<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    fun asLiveData() = result as LiveData<Result<ResultType>>

    protected fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job =
        GlobalScope.launch(
            context = context,
            block = block
        )

    protected fun launchMainThread(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(
            context = Dispatchers.Main,
            block = block
        )

    protected fun launchWorkerThread(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(
            context = Dispatchers.IO,
            block = block
        )

    protected suspend fun <T> mainThread(block: suspend CoroutineScope.() -> T): T =
        withContext(
            context = Dispatchers.Main,
            block = block
        )

    protected suspend fun <T> workerThread(block: suspend CoroutineScope.() -> T): T =
        withContext(
            context = Dispatchers.IO,
            block = block
        )

    @MainThread
    protected abstract fun fetchFromNetwork(dbSource: LiveData<ResultType>)

    @WorkerThread
    protected abstract fun saveCallResult(item: ResultType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): Response<ResponseType>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<ResponseType>) = response.body

    @WorkerThread
    protected abstract fun convertToResultType(response: ResponseType): ResultType

    @WorkerThread
    protected abstract fun callFailed(errorMessage: String)
}
