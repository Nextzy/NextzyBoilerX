package com.nextzy.library.boilerx.repository.core.experiment

import com.google.gson.annotations.SerializedName
import retrofit2.Call

class TestWrapper(
    private val tokenRefresher: PreTokenRefresher<AccessTokenResponse>,
    private val tokenStore: AwesomeTokenStore,
    private val tokenUpdater: AwesomeTokenUpdater
) {
    fun awesome() {

    }

    fun handleRefreshToken(body: () -> Unit) {

    }

    fun retry(body: () -> Unit) {

    }


    fun accessTokenValidator() {

    }

    fun tokenRefresher() {

    }

    fun isTokenExpired(token: String?): Boolean {
        return true
    }

    suspend fun run() {
        val apiManager = ApiManager()
//        val tokenStore = AwesomeTokenStore()
//        val tokenUpdater = AwesomeTokenUpdater(Util(), apiManager)
//        val tokenRefresher = PreTokenRefresher<AccessTokenResponse>()
        val call: Call<String> = tokenRefresher.execute({
            // TODO Call service
            apiManager.profile().getProfile()
        })
        call.execute()


    }
}

interface RequestExecutor<Response> {
    suspend fun onNext(): Call<Response>
}

interface TokenUpdater<T> {
    suspend fun onUpdateNewToken(): Call<T>

    fun onTokenUpdateSuccess(body: T?)

    fun onTokenUpdateFailure(code: Int, message: String?): Exception
}

interface TokenStore {
    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    fun isTokenExpired(token: String?): Boolean
}

class PreTokenRefresher<TokenData>(
    private val defaultStore: TokenStore,
    private val defaultUpdater: TokenUpdater<TokenData>
) {
    suspend fun <Response> execute(
        onNext: suspend () -> Call<Response>,
        customStore: TokenStore? = null,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Call<Response> {
        return execute(object : RequestExecutor<Response> {
            override suspend fun onNext(): Call<Response> = onNext.invoke()
        }, customStore, customUpdater)
    }

    suspend fun <Response> execute(
        requestExecutor: RequestExecutor<Response>,
        customStore: TokenStore? = null,
        customUpdater: TokenUpdater<TokenData>? = null
    ): Call<Response> {
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