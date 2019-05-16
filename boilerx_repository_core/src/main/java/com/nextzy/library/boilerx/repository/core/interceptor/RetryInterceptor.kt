package com.nextzy.library.boilerx.repository.core.interceptor

import android.os.Bundle

interface RetryInterceptor {
    fun shouldRetry(e: Exception, retry: Int, data: Bundle?): Boolean
}