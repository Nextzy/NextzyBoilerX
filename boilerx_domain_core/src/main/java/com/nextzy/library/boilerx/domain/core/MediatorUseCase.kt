package com.nextzy.library.boilerx.domain.core

import androidx.lifecycle.MediatorLiveData
import com.nextzy.library.boilerx.repository.core.vo.Result

/**
 * Executes business logic in its execute method and keep posting updates to the result as
 * [Result<R>].
 * Handling an exception (emit [Result.Error] to the result) is the subclasses's responsibility.
 */
abstract class MediatorUseCase<in P, R> {
    protected val result = MediatorLiveData<Result<R>>()

    // Make this as open so that mock instances can mock this method
    open fun observe(): MediatorLiveData<Result<R>> {
        return result
    }

    abstract fun execute(parameters: P)
}
