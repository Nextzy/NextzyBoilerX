package com.nextzy.library.boilerx.repository.core.token

import retrofit2.Call

interface RequestExecutor<Response> {
    suspend fun onNext(): Call<Response>
}
