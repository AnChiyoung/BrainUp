package com.dev.goodluckcy.brainup.domain.model

/** 게임별 개인 최고 기록 */
data class BestRecord(
    val gameType: GameType,
    val score: Int,
    val level: Int,
    val medianReactionMs: Long?,
    val playedAtMs: Long,
)
