package com.dev.goodluckcy.brainup.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_record",
    indices = [Index("gameType"), Index("playedAt")],
)
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** GameType.name */
    val gameType: String,
    val score: Int,
    /** 최고 도달 레벨(패턴 기억은 최대 기억 길이) */
    val level: Int,
    val roundsCleared: Int,
    val durationMs: Long,
    /** 게임 종료 시각(epoch ms) */
    val playedAt: Long,
    val medianReactionMs: Long? = null,
    val bestReactionMs: Long? = null,
)

/** 기간 내 게임별 최고 점수 조회 결과 */
data class GameTypeBestScore(
    val gameType: String,
    val bestScore: Int,
)
