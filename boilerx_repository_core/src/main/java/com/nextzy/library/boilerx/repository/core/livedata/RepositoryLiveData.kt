package com.nextzy.library.boilerx.repository.core.livedata

import android.os.Bundle
import androidx.lifecycle.LiveData
import com.nextzy.library.boilerx.network.core.ApiResponse
import com.nextzy.library.boilerx.repository.core.interceptor.RetryInterceptor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response


class RepositoryLiveData<T>(
    private var deferred: Deferred<Response<T>>,
    private var retryInterceptor: RetryInterceptor?,
    private var data: Bundle?,
    private var maxRetry: Int = 1
) : LiveData<ApiResponse<T>>() {

    fun execute() {
        GlobalScope.launch {
            for (retry in 0..(maxRetry - 1)) {
                try {
                    val result: Response<T> = deferred.await()
                    postValue(ApiResponse.create(result))
                    break
                } catch (e: Exception) {
                    if (shouldRetry(e, retry)) {
                        postValue(ApiResponse.create(e))
                        break
                    }
                }
            }
        }
    }

    private fun shouldRetry(e: Exception, retry: Int) =
        retryInterceptor?.shouldRetry(e, retry, data) ?: true && isNotLastRetryTime(retry)

    private fun isNotLastRetryTime(retry: Int) = retry == (maxRetry - 1)
}