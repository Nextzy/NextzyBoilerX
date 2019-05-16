package com.nextzy.library.boilerx.repository.core.interceptor

import com.nextzy.library.boilerx.network.core.RequestContainer

interface ErrorResponseInterceptor<InputType, RequestType> {
    fun onResponseFailure(
        requestContainer: RequestContainer<InputType, RequestType>,
        errorMessage: String?
    )
}