package com.nextzy.library.boilerx.network.grpc

import io.grpc.stub.StreamObserver
import retrofit2.Response
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StreamObserverAdapter<T>(private var continuation: Continuation<Response<T>>) : StreamObserver<T> {
    override fun onNext(value: T) {
        continuation.resume(Response.success(value))
    }

    override fun onError(t: Throwable) {
        continuation.resumeWithException(t)
    }

    override fun onCompleted() {
    }
}
