package com.dev.goodluckcy.brainup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dev.goodluckcy.brainup.data.local.entity.GameRecordEntity
import com.dev.goodluckcy.brainup.data.local.entity.GameTypeBestScore
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert
    suspend fun insert(record: GameRecordEntity): Long

    @Query("SELECT MAX(score) FROM game_record WHERE gameType = :gameType")
    suspend fun maxScore(gameType: String): Int?

    @Query(
        """
        SELECT gameType, MAX(score) AS bestScore FROM game_record
        WHERE playedAt >= :fromMs AND playedAt < :toMs
        GROUP BY gameType
        """,
    )
    suspend fun bestScoresBetween(fromMs: Long, toMs: Long): List<GameTypeBestScore>

    /** 게임별 최고 점수 기록 1건씩. 동점이면 먼저 달성한 기록. */
    @Query(
        """
        SELECT * FROM game_record WHERE id IN (
            SELECT (
                SELECT id FROM game_record AS best
                WHERE best.gameType = g.gameType
                ORDER BY best.score DESC, best.playedAt ASC
                LIMIT 1
            )
            FROM game_record AS g
            GROUP BY g.gameType
        )
        """,
    )
    fun observeBestRecords(): Flow<List<GameRecordEntity>>
}
