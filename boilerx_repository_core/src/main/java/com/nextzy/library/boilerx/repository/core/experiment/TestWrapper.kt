package com.nextzy.library.boilerx.repository.core.experiment

import androidx.annotation.WorkerThread
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response

class TestWrapper(
    private val preTokenRefresher: PreTokenRefresher<AccessTokenResponse>,
    private val postTokenRefresher: PostTokenRefresher<AccessTokenResponse>,
    private val tokenStore: AwesomeTokenStore,
    private val tokenUpdater: AwesomeTokenUpdater
) {

    fun retry(body: () -> Unit) {

    }

    suspend fun run() {
        val postRefresher = PostTokenRefresher(
            AwesomeTokenUpdater(Util(), ApiManager()),
            AwesomeTokenExpiredResponseValidator()
        )
        val apiManager = ApiManager()
//        val tokenStore = AwesomeTokenStore()
//        val tokenUpdater = AwesomeTokenUpdater(Util(), apiManager)
//        val tokenRefresher = PreTokenRefresher<AccessTokenResponse>()

    }
}


class AwesomeApiCaller(
    private val preTokenRefresher: PreTokenRefresher<AccessTokenResponse>? = null,
    private val postTokenRefresher: PostTokenRefresher<AccessTokenResponse>? = null,
    private val apiManager: ApiManager
) : ApiCaller<AccessTokenResponse>(preTokenRefresher, postTokenRefresher) {
    suspend fun getProfile() {
        retry(3) {
            postTokenRefresh {
                preTokenRefresh {
                    apiManager.profile().getProfile()
                }
            }
        }
    }

    private suspend fun retry(retry: Int, function: suspend () -> Response<String>) {
        val response = function.invoke()
    }
}

open class ApiCaller<TokenData>(
    private val preTokenRefresher: PreTokenRefresher<TokenData>?,
    private val postTokenRefresher: PostTokenRefresher<TokenData>?
) {
    suspend fun preTokenRefresh(body: suspend () -> Call<String>): Call<String> {
        return preTokenRefresher?.let { refresher ->
            refresher.execute({ body.invoke() })
        } ?: run {
            return body.invoke()
        }
    }

    suspend fun postTokenRefresh(body: suspend () -> Call<String>): Response<String> {
        return postTokenRefresher?.let { refresher ->
            refresher.execute({ body.invoke() })
        } ?: run {
            return body.invoke().execute()
        }
    }
}

interface RequestExecutor<Response> {
    suspend fun onNext(): Call<Response>
}

interface TokenUpdater<TokenData> {
    suspend fun onUpdateNewToken(): Call<TokenData>

    fun onTokenUpdateSuccess(body: TokenData?)

    fun onTokenUpdateFailure(code: Int, message: String?): Exception
}

interface TokenStore {
    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    fun isTokenExpired(token: String?): Boolean
}

interface TokenExpiredResponseValidator {
    fun <T> validate(response: Response<T>): Boolean
}

data class ErrorResponse(
    val message: String?,
    val status: String?
)

class AwesomeTokenExpiredResponseValidator : TokenExpiredResponseValidator {
    override fun <T> validate(response: Response<T>): Boolean {
        return when (response.body()) {
            is ErrorResponse -> {
                response.code() == 401 && (response.body() as ErrorResponse).status == "MYCHN002"
            }
            else -> false
        }
    }
}

//interface RetryController {
//    fun shouldRetry(response: Response<Any>): Boolean
//}
//
//class AwesomeRetryController : RetryController {
//    override fun shouldRetry(response: Response<Any>): Boolean {
//        return true
//    }
//}

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

class AwesomeTokenStore : TokenStore {
    override fun getAccessToken(): String? = "1234"

    override fun getRefreshToken(): String? = "1234"

    override fun isTokenExpired(token: String?): Boolean = true
}


class AwesomeTokenUpdater(
    private val util: Util,
    private val apiManager: ApiManager
) : TokenUpdater<AccessTokenResponse> {
    override suspend fun onUpdateNewToken(): Call<AccessTokenResponse> {
        return apiManager.token().refreshToken()
    }

    override fun onTokenUpdateSuccess(body: AccessTokenResponse?) {
        util.saveNewAccessTokenToDatabase(body?.data)
    }

    override fun onTokenUpdateFailure(code: Int, message: String?): Exception {
        return TokenUpdateFailedException("So bad")
    }
}

class TokenUpdateFailedException(override val message: String?) : NullPointerException(message)

class Util {
    fun doSomething() {
        // Nothing
    }

    fun callRefreshTokenService() {
        // Nothing
    }

    fun saveNewAccessTokenToDatabase(data: AccessToken?) {
        // Nothing
    }
}

data class AccessTokenResponse(
    @SerializedName("resultCode")
    val resultCode: String?,
    @SerializedName("resultDescription")
    val resultDescription: String?,
    @SerializedName("developerMessage")
    val developerMessage: String?,
    @SerializedName("data")
    val data: AccessToken?
)

data class AccessToken(
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?
)