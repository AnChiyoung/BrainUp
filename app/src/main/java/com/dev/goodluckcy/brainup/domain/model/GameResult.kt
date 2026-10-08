package com.dev.goodluckcy.brainup.domain.model

/** 게임 정상 종료 시 결과. GameRecord(Room) 저장과 결과 화면 표시에 사용한다. */
data class GameResult(
    val gameType: GameType,
    val score: Int,
    val level: Int,
    val roundsCleared: Int,
    val durationMs: Long,
)
