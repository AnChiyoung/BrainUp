package com.dev.goodluckcy.brainup.core.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {

    private val now = 10_000_000L
    private val fiveMinutes = InterstitialPolicy.MIN_INTERVAL_MS

    private fun canShow(
        loaded: Boolean = true,
        games: Int = 3,
        lastShownAtMs: Long? = null,
    ) = InterstitialPolicy.canShow(loaded, games, lastShownAtMs, now)

    @Test
    fun `shows after three games when never shown before`() {
        assertTrue(canShow())
    }

    @Test
    fun `does not show before three games`() {
        assertFalse(canShow(games = 2))
    }

    @Test
    fun `does not show when ad is not loaded`() {
        assertFalse(canShow(loaded = false))
    }

    @Test
    fun `does not show within five minutes of last ad`() {
        assertFalse(canShow(games = 10, lastShownAtMs = now - fiveMinutes + 1))
    }

    @Test
    fun `shows when five minutes passed and three games played`() {
        assertTrue(canShow(lastShownAtMs = now - fiveMinutes))
    }

    @Test
    fun `clock moved backwards does not block ads forever`() {
        assertTrue(canShow(lastShownAtMs = now + 60_000))
    }
}
