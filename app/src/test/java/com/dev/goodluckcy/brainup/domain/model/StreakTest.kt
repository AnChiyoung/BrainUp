package com.dev.goodluckcy.brainup.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val today = LocalDate.of(2026, 10, 9)
    private fun daysAgo(vararg days: Long) = days.map { today.minusDays(it) }

    @Test
    fun `no completed days is zero`() {
        assertEquals(0, Streak.current(emptyList(), today))
    }

    @Test
    fun `counts consecutive days ending today`() {
        assertEquals(3, Streak.current(daysAgo(0, 1, 2), today))
    }

    @Test
    fun `streak through yesterday is kept while today is not completed yet`() {
        assertEquals(2, Streak.current(daysAgo(1, 2), today))
    }

    @Test
    fun `missing yesterday breaks streak`() {
        assertEquals(0, Streak.current(daysAgo(2, 3, 4), today))
    }

    @Test
    fun `stops at first gap`() {
        assertEquals(2, Streak.current(daysAgo(0, 1, 3, 4, 5), today))
    }

    @Test
    fun `order and duplicates do not matter`() {
        assertEquals(3, Streak.current(daysAgo(2, 0, 1, 0), today))
    }

    @Test
    fun `future dates are ignored`() {
        assertEquals(1, Streak.current(listOf(today, today.plusDays(1)), today))
    }

    @Test
    fun `shielded day keeps the chain but is not counted`() {
        assertEquals(3, Streak.current(daysAgo(0, 2, 3), today, shieldedDates = daysAgo(1)))
    }

    @Test
    fun `shielded yesterday keeps streak alive before today is played`() {
        assertEquals(2, Streak.current(daysAgo(2, 3), today, shieldedDates = daysAgo(1)))
    }

    @Test
    fun `shields alone do not make a streak`() {
        assertEquals(0, Streak.current(emptyList(), today, shieldedDates = daysAgo(1, 2)))
    }
}
