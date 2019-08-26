package com.nextzy.library.boilerx.repository.core.experiment

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.POST

interface TokenService {
    @POST("token/refreshToken")
    suspend fun refreshToken(): Call<AccessTokenResponse>
}

interface ProfileService {
    @POST("profile/me")
    suspend fun getProfile(): Call<String>
}

class ApiManager {
    fun token(): TokenService {
        return Retrofit.Builder().apply {
            // Custom Retrofit Builder
        }.build().create(TokenService::class.java)
    }

    fun profile(): ProfileService {
        return Retrofit.Builder().apply {
            // Custom Retrofit Builder
        }.build().create(ProfileService::class.java)
    }
}