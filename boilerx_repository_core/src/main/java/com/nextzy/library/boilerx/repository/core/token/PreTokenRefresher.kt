package com.nextzy.library.boilerx.repository.core.token

import androidx.annotation.WorkerThread
import retrofit2.Call

class PreTokenRefresher<TokenData>(
    private val defaultStore: TokenStore,
    private val defaultUpdater: TokenUpdater<TokenData>
) {
    @WorkerThread
    suspend fun <ApiResponse> execute(
        onNext: suspend () -> Call<ApiResponse>,
        customStore: TokenStore? = null,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Call<ApiResponse> {
        return execute(object : RequestExecutor<ApiResponse> {
            override suspend fun onNext(): Call<ApiResponse> = onNext.invoke()
        }, customStore, customUpdater)
    }

    @WorkerThread
    suspend fun <ApiResponse> execute(
        requestExecutor: RequestExecutor<ApiResponse>,
        customStore: TokenStore? = null,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Call<ApiResponse> {
        val store = customStore ?: defaultStore
        val updater = customUpdater ?: defaultUpdater
        return if (store.isTokenExpired(store.getAccessToken())) {
            val response = updater.onUpdateNewToken().execute()
            if (response.isSuccessful) {
                updater.onTokenUpdateSuccess(response.body())
                requestExecutor.onNext()
            } else {
                throw updater.onTokenUpdateFailure(response.code(), response.message())
            }
        } else {
            requestExecutor.onNext()
        }
    }
}
