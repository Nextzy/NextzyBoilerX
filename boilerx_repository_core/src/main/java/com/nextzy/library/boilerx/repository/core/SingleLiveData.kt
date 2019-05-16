package com.nextzy.library.boilerx.repository.core

import androidx.lifecycle.LiveData

class SingleLiveData<T>(input: T) : LiveData<T>() {
    init {
        postValue(input)
    }
}