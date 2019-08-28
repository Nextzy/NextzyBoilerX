package com.nextzy.library.boilerx.repository.core.token

import retrofit2.Response

interface TokenExpiredResponseValidator {
    fun <T> validate(response: Response<T>): Boolean
}