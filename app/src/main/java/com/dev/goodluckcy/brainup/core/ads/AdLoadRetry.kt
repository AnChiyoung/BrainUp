package com.dev.goodluckcy.brainup.core.ads

/** 광고 로드 실패 시 재시도 간격(지수 증가). 성공하면 [reset]한다. */
class AdLoadRetry(
    private val initialDelayMs: Long = 30_000L,
    private val maxDelayMs: Long = 10 * 60_000L,
) {
    private var nextDelayMs = initialDelayMs

    fun nextDelay(): Long = nextDelayMs.also {
        nextDelayMs = (nextDelayMs * 2).coerceAtMost(maxDelayMs)
    }

    fun reset() {
        nextDelayMs = initialDelayMs
    }
}
