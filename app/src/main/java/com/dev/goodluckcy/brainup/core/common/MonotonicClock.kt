package com.dev.goodluckcy.brainup.core.common

import android.os.SystemClock
import javax.inject.Inject

/** 기기 슬립 시간을 포함하는 단조 시계. 경과 시간 측정에만 사용한다. */
interface MonotonicClock {
    fun elapsedRealtimeMs(): Long
    fun elapsedRealtimeNanos(): Long
}

class SystemMonotonicClock @Inject constructor() : MonotonicClock {
    override fun elapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()
    override fun elapsedRealtimeNanos(): Long = SystemClock.elapsedRealtimeNanos()
}
