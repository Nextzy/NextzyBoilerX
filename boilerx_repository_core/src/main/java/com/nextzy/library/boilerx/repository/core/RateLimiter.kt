package com.nextzy.library.boilerx.repository.core

import android.os.SystemClock
import androidx.collection.ArrayMap
import java.util.concurrent.TimeUnit

class RateLimiter<KEY> {
    protected var timestamps: MutableMap<KEY, Long> = ArrayMap()
    private var timeout: Long? = null

    fun RateLimiter(timeout: Int, timeUnit: TimeUnit) {
        this.timeout = timeUnit.toMillis(timeout.toLong())
    }

    @Synchronized
    fun shouldFetch(key: KEY): Boolean {
        val lastFetched = timestamps[key]
        val now = now()
        if (lastFetched == null) {
            timestamps[key] = now
            return onNullLastFetched(key, timestamps)
        }
        if (now - lastFetched > this.timeout!!) {
            timestamps[key] = now
            return true
        }
        return false
    }

    fun clear(key: KEY) {
        timestamps[key] = 0L
    }

    fun clearAll() {
        timestamps.clear()
    }

    fun onNullLastFetched(key: KEY, timestamps: Map<KEY, Long>): Boolean {
        return true
    }

    protected fun now(): Long {
        return SystemClock.uptimeMillis()
    }

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }
}
