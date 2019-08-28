package com.nextzy.library.boilerx.repository.core.token

import androidx.annotation.WorkerThread
import retrofit2.Call
import retrofit2.Response

class PostTokenRefresher<TokenData>(
    private val defaultUpdater: TokenUpdater<TokenData>,
    private val tokenExpiredValidator: TokenExpiredResponseValidator
) {
    @WorkerThread
    suspend fun <ApiResponse> execute(
        onNext: suspend () -> Call<ApiResponse>,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Response<ApiResponse> {
        return execute(object : RequestExecutor<ApiResponse> {
            override suspend fun onNext(): Call<ApiResponse> = onNext.invoke()
        }, customUpdater)
    }

    @WorkerThread
    suspend fun <ApiResponse> execute(
        requestExecutor: RequestExecutor<ApiResponse>,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Response<ApiResponse> {
        val updater = customUpdater ?: defaultUpdater
        val response = requestExecutor.onNext().execute()
        return if (!tokenExpiredValidator.validate(response)) {
            val refreshTokenResponse = updater.onUpdateNewToken().execute()
            updater.onTokenUpdateSuccess(refreshTokenResponse.body())
            requestExecutor.onNext().execute()
        } else {
            response
        }
    }
}
