package com.nextzy.library.boilerx.domain.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.nextzy.library.boilerx.repository.core.vo.Result

fun <X, Y> LiveData<X>.map(body: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, body)
}

/** Uses `Transformations.switchMap` on a LiveData */
fun <X, Y> LiveData<X>.switchMap(body: (X) -> LiveData<Y>): LiveData<Y> {
    return Transformations.switchMap(this, body)
}

fun <X, Y> LiveData<X>.then(liveData: MediatorLiveData<Y>, body: (X?) -> Unit) {
    liveData.addSource(this) { value: X ->
        body.invoke(value)
    }
}

fun <X, Y> LiveData<Result<X>>.convert(
    liveData: MediatorLiveData<Y>,
    success: ((data: X?) -> Unit)? = null,
    failure: ((exception: Exception) -> Unit)? = null,
    loading: (() -> Unit)? = null
) {
    liveData.addSource(this) { result: Result<X> ->
        when (result) {
            is Result.Success -> success?.invoke(result.data)
            is Result.Error -> failure?.invoke(result.exception)
            is Result.Loading -> loading?.invoke()
        }
    }
}

fun <X, Y> LiveData<Result<X>>.chain(
    liveData: MediatorLiveData<Y>,
    success: ((data: X) -> Unit)? = null,
    failure: ((exception: Exception) -> Unit)? = null
) {
    liveData.addSource(this) { result: Result<X> ->
        when (result) {
            is Result.Success -> {
                result.data?.let { data ->
                    success?.invoke(data)
                }
            }
            is Result.Error -> failure?.invoke(result.exception)
        }
    }
}