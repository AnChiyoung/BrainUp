package com.dev.goodluckcy.brainup.data.repository

import androidx.room.withTransaction
import com.dev.goodluckcy.brainup.core.common.LocalDateProvider
import com.dev.goodluckcy.brainup.data.local.BrainUpDatabase
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.dao.GameRecordDao
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.data.local.entity.GameRecordEntity
import com.dev.goodluckcy.brainup.domain.model.BestRecord
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.RecordOutcome
import com.dev.goodluckcy.brainup.domain.repository.GameRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineGameRecordRepository @Inject constructor(
    private val database: BrainUpDatabase,
    private val gameRecordDao: GameRecordDao,
    private val dailyProgressDao: DailyProgressDao,
    private val dateProvider: LocalDateProvider,
) : GameRecordRepository {

    override suspend fun record(result: GameResult): RecordOutcome = database.withTransaction {
        val playedAt = dateProvider.nowMs()
        val previousBest = gameRecordDao.maxScore(result.gameType.name)
        gameRecordDao.insert(result.toEntity(playedAt))

        // 같은 게임을 여러 번 해도 완료 개수는 늘지 않고, 총점은 게임별 그날 최고 점수의 합이다.
        val date = dateProvider.dateOf(playedAt)
        val todayBest = gameRecordDao.bestScoresBetween(
            fromMs = dateProvider.startOfDayMs(date),
            toMs = dateProvider.startOfDayMs(date.plusDays(1)),
        )
        val mask = todayBest
            .mapNotNull { gameTypeOf(it.gameType) }
            .fold(0) { acc, gameType -> acc or gameType.bit }
        val completed = mask == GameType.ALL_COMPLETED_MASK
        val wasCompleted = dailyProgressDao.get(date.toString())?.completed == true
        val totalScore = todayBest.sumOf { it.bestScore }
        dailyProgressDao.upsert(
            DailyProgressEntity(
                date = date.toString(),
                completedMask = mask,
                totalScore = totalScore,
                completed = completed,
            ),
        )

        RecordOutcome(
            isPersonalBest = previousBest == null || result.score > previousBest,
            previousBestScore = previousBest,
            dailyCompletedNow = completed && !wasCompleted,
            todayCompletedCount = Integer.bitCount(mask),
            todayTotalScore = totalScore,
        )
    }

    override fun observeBestRecords(): Flow<List<BestRecord>> =
        gameRecordDao.observeBestRecords().map { records ->
            records.mapNotNull { record ->
                val gameType = gameTypeOf(record.gameType) ?: return@mapNotNull null
                BestRecord(
                    gameType = gameType,
                    score = record.score,
                    level = record.level,
                    medianReactionMs = record.medianReactionMs,
                    playedAtMs = record.playedAt,
                )
            }.sortedBy { it.gameType.ordinal }
        }

    private fun GameResult.toEntity(playedAt: Long) = GameRecordEntity(
        gameType = gameType.name,
        score = score,
        level = level,
        roundsCleared = roundsCleared,
        durationMs = durationMs,
        playedAt = playedAt,
        medianReactionMs = medianReactionMs,
        bestReactionMs = bestReactionMs,
    )
}

internal fun gameTypeOf(name: String): GameType? = GameType.entries.find { it.name == name }
