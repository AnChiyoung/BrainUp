package com.dev.goodluckcy.brainup.testing

import com.dev.goodluckcy.brainup.core.common.MonotonicClock

class FakeMonotonicClock : MonotonicClock {
    var nowNanos = 0L

    var nowMs: Long
        get() = nowNanos / 1_000_000
        set(value) {
            nowNanos = value * 1_000_000
        }

    fun advanceMs(ms: Long) {
        nowNanos += ms * 1_000_000
    }

    override fun elapsedRealtimeMs(): Long = nowMs
    override fun elapsedRealtimeNanos(): Long = nowNanos
}
