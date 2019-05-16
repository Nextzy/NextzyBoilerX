package com.nextzy.library.boilerx.repository.core.livedata

import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleEventLiveData<T> : MutableLiveData<T>() {
    private val mPending = AtomicBoolean(false)
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { t: T? ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })

    }
    @MainThread
    override fun setValue(@Nullable t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    @MainThread
    override fun postValue(value: T?) {
        mPending.set(true)
        super.postValue(value)
    }

}