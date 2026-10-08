package com.dev.goodluckcy.brainup.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dev.goodluckcy.brainup.core.common.LocalDateProvider
import com.dev.goodluckcy.brainup.data.local.BrainUpDatabase
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class OfflineRepositoriesTest {

    private lateinit var database: BrainUpDatabase
    private lateinit var gameRecords: OfflineGameRecordRepository
    private lateinit var dailyChallenge: OfflineDailyChallengeRepository
    private val clock = MutableClock(Instant.EPOCH)
    private val today = LocalDate.of(2026, 10, 9)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrainUpDatabase::class.java,
        ).build()
        val dateProvider = LocalDateProvider(clock)
        gameRecords = OfflineGameRecordRepository(
            database = database,
            gameRecordDao = database.gameRecordDao(),
            dailyProgressDao = database.dailyProgressDao(),
            dateProvider = dateProvider,
        )
        dailyChallenge = OfflineDailyChallengeRepository(database.dailyProgressDao(), dateProvider)
        playAt(today, hour = 12)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun playAt(date: LocalDate, hour: Int, minute: Int = 0) {
        clock.setLocal(LocalDateTime.of(date, LocalTime.of(hour, minute)))
    }

    private fun result(gameType: GameType, score: Int) = GameResult(
        gameType = gameType,
        score = score,
        level = 2,
        roundsCleared = 3,
        durationMs = 30_000,
        medianReactionMs = if (gameType == GameType.REACTION) 1_000L - score else null,
        bestReactionMs = if (gameType == GameType.REACTION) 900L - score else null,
    )

    private suspend fun completeAllGames(score: Int = 100) {
        GameType.entries.forEach { gameRecords.record(result(it, score)) }
    }

    @Test
    fun firstRecordIsPersonalBest() = runTest {
        val outcome = gameRecords.record(result(GameType.NUMBER_MEMORY, 50))
        assertTrue(outcome.isPersonalBest)
        assertNull(outcome.previousBestScore)
    }

    @Test
    fun onlyHigherScoreIsPersonalBest() = runTest {
        gameRecords.record(result(GameType.NUMBER_MEMORY, 50))

        val lower = gameRecords.record(result(GameType.NUMBER_MEMORY, 40))
        assertFalse(lower.isPersonalBest)
        assertEquals(50, lower.previousBestScore)

        val tie = gameRecords.record(result(GameType.NUMBER_MEMORY, 50))
        assertFalse(tie.isPersonalBest)

        val higher = gameRecords.record(result(GameType.NUMBER_MEMORY, 70))
        assertTrue(higher.isPersonalBest)
        assertEquals(50, higher.previousBestScore)
    }

    @Test
    fun repeatingSameGameDoesNotIncreaseCompletedCount() = runTest {
        gameRecords.record(result(GameType.REACTION, 600))
        val outcome = gameRecords.record(result(GameType.REACTION, 700))

        assertEquals(1, outcome.todayCompletedCount)
        val progress = dailyChallenge.observeToday().first()
        assertEquals(setOf(GameType.REACTION), progress.completedGames)
        assertFalse(progress.isCompleted)
    }

    @Test
    fun todayScoreIsSumOfBestScorePerGame() = runTest {
        gameRecords.record(result(GameType.NUMBER_MEMORY, 30))
        gameRecords.record(result(GameType.NUMBER_MEMORY, 90))
        gameRecords.record(result(GameType.NUMBER_MEMORY, 60))
        gameRecords.record(result(GameType.REACTION, 700))

        assertEquals(790, dailyChallenge.observeToday().first().totalScore)
    }

    @Test
    fun dailyCompletedNowIsReportedOnlyOnce() = runTest {
        gameRecords.record(result(GameType.NUMBER_MEMORY, 10))
        gameRecords.record(result(GameType.REACTION, 10))
        val completing = gameRecords.record(result(GameType.PATTERN, 10))
        assertTrue(completing.dailyCompletedNow)
        assertEquals(3, completing.todayCompletedCount)

        val again = gameRecords.record(result(GameType.PATTERN, 20))
        assertFalse(again.dailyCompletedNow)
        assertTrue(dailyChallenge.observeToday().first().isCompleted)
    }

    @Test
    fun gamesPlayedOnDifferentDaysDoNotCompleteChallenge() = runTest {
        playAt(today.minusDays(1), hour = 23, minute = 59)
        gameRecords.record(result(GameType.NUMBER_MEMORY, 10))
        gameRecords.record(result(GameType.REACTION, 10))
        playAt(today, hour = 0, minute = 1)
        val outcome = gameRecords.record(result(GameType.PATTERN, 10))

        assertFalse(outcome.dailyCompletedNow)
        assertEquals(1, outcome.todayCompletedCount)
    }

    @Test
    fun streakCountsConsecutiveCompletedDays() = runTest {
        for (daysAgo in 3L downTo 1L) {
            playAt(today.minusDays(daysAgo), hour = 20)
            completeAllGames()
        }
        playAt(today, hour = 9)
        assertEquals(3, dailyChallenge.observeStreak().first())

        completeAllGames()
        assertEquals(4, dailyChallenge.observeStreak().first())
    }

    @Test
    fun recentDaysFillsMissingDatesOldestFirst() = runTest {
        playAt(today.minusDays(2), hour = 10)
        completeAllGames(score = 100)
        playAt(today, hour = 10)
        gameRecords.record(result(GameType.REACTION, 500))

        val days = dailyChallenge.observeRecentDays(7).first()
        assertEquals(7, days.size)
        assertEquals(today.minusDays(6), days.first().date)
        assertEquals(today, days.last().date)
        assertEquals(300, days[4].totalScore)
        assertTrue(days[4].isCompleted)
        assertEquals(500, days[6].totalScore)
        assertEquals(0, days[5].totalScore)
    }

    @Test
    fun bestRecordsReturnHighestScorePerGame() = runTest {
        gameRecords.record(result(GameType.NUMBER_MEMORY, 40))
        gameRecords.record(result(GameType.NUMBER_MEMORY, 90))
        gameRecords.record(result(GameType.REACTION, 700))
        gameRecords.record(result(GameType.REACTION, 650))

        val best = gameRecords.observeBestRecords().first().associateBy { it.gameType }
        assertEquals(setOf(GameType.NUMBER_MEMORY, GameType.REACTION), best.keys)
        assertEquals(90, best.getValue(GameType.NUMBER_MEMORY).score)
        assertEquals(700, best.getValue(GameType.REACTION).score)
        assertEquals(300L, best.getValue(GameType.REACTION).medianReactionMs)
    }
}
