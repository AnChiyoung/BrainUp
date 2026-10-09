package com.dev.goodluckcy.brainup.core.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class AdLoadRetryTest {

    @Test
    fun `delay doubles up to maximum`() {
        val retry = AdLoadRetry(initialDelayMs = 1_000, maxDelayMs = 5_000)
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 5_000L, 5_000L), List(5) { retry.nextDelay() })
    }

    @Test
    fun `reset starts from initial delay again`() {
        val retry = AdLoadRetry(initialDelayMs = 1_000, maxDelayMs = 5_000)
        repeat(3) { retry.nextDelay() }
        retry.reset()
        assertEquals(1_000L, retry.nextDelay())
    }
}
