package com.dev.goodluckcy.brainup.domain.model

/** 게임 정상 종료 시 결과. GameRecord(Room) 저장과 결과 화면 표시에 사용한다. */
data class GameResult(
    val gameType: GameType,
    val score: Int,
    val level: Int,
    val roundsCleared: Int,
    val durationMs: Long,
    /** 반응 속도 게임 전용: 유효 시도의 중앙값 */
    val medianReactionMs: Long? = null,
    /** 반응 속도 게임 전용: 유효 시도 중 최단 시간 */
    val bestReactionMs: Long? = null,
)
