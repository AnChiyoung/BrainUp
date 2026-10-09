package com.dev.goodluckcy.brainup.data.repository

import com.dev.goodluckcy.brainup.core.common.LocalDateProvider
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.domain.model.DailyProgress
import com.dev.goodluckcy.brainup.domain.model.Streak
import com.dev.goodluckcy.brainup.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineDailyChallengeRepository @Inject constructor(
    private val dailyProgressDao: DailyProgressDao,
    private val dateProvider: LocalDateProvider,
) : DailyChallengeRepository {

    override fun observeToday(): Flow<DailyProgress> =
        dateProvider.observeToday().flatMapLatest { today ->
            dailyProgressDao.observe(today.toString()).map { it?.toDomain() ?: DailyProgress(today) }
        }

    override fun observeStreak(): Flow<Int> =
        combine(dateProvider.observeToday(), dailyProgressDao.observeStreakDays()) { today, days ->
            Streak.current(
                completedDates = days.filter { it.completed }.map { LocalDate.parse(it.date) },
                today = today,
                shieldedDates = days.filter { it.shielded }.map { LocalDate.parse(it.date) },
            )
        }

    override fun observeRecentDays(days: Int): Flow<List<DailyProgress>> {
        require(days > 0) { "days must be positive: $days" }
        return dateProvider.observeToday().flatMapLatest { today ->
            val from = today.minusDays(days - 1L)
            dailyProgressDao.observeRange(from.toString(), today.toString()).map { entities ->
                val byDate = entities.associateBy { it.date }
                (0 until days).map { offset ->
                    val date = from.plusDays(offset.toLong())
                    byDate[date.toString()]?.toDomain() ?: DailyProgress(date)
                }
            }
        }
    }

    private fun DailyProgressEntity.toDomain() = DailyProgress(
        date = LocalDate.parse(date),
        completedGames = DailyProgress.completedGamesOf(completedMask),
        totalScore = totalScore,
        shielded = shielded,
    )
}
