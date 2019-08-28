package com.nextzy.library.boilerx.repository.core

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nextzy.library.boilerx.network.core.ApiEmptyResponse
import com.nextzy.library.boilerx.network.core.ApiErrorResponse
import com.nextzy.library.boilerx.network.core.ApiResponse
import com.nextzy.library.boilerx.network.core.ApiSuccessResponse
import com.nextzy.library.boilerx.repository.core.vo.Result
import kotlinx.coroutines.Dispatchers

abstract class NetworkBoundResource2<ResultType, ResponseType> :
    DataBoundResource2<ResultType, ResponseType>() {
    @MainThread
    override fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        setValue(Result.Loading)
        val apiResponse: LiveData<ApiResponse<ResponseType>> = liveData(Dispatchers.IO) {
            try {
                val response = createCall()
                emit(ApiResponse.create(response))
            } catch (e: Exception) {
                emit(ApiResponse.create<ResponseType>(e))
            }
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    launchWorkerThread {
                        val data: ResultType = convertToResultType(processResponse(response))
                        saveCallResult(data)
                        mainThread {
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Result.Success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    launchMainThread {
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Result.Success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    launchWorkerThread {
                        callFailed(response.errorMessage)
                        mainThread {
                            setValue(Result.Error(Exception(response.errorMessage)))
                        }
                    }
                }
            }
        }
    }
}