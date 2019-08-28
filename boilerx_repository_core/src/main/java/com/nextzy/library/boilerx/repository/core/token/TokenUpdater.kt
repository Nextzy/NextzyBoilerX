package com.nextzy.library.boilerx.repository.core.token

import retrofit2.Call

interface TokenUpdater<TokenData> {
    suspend fun onUpdateNewToken(): Call<TokenData>

    fun onTokenUpdateSuccess(body: TokenData?)

    fun onTokenUpdateFailure(code: Int, message: String?): Exception
}
