package com.nextzy.library.boilerx.repository.core.token

interface TokenStore {
    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    fun isTokenExpired(token: String?): Boolean
}
