package com.dev.goodluckcy.brainup.domain.model

import java.time.LocalDate

data class DailyProgress(
    val date: LocalDate,
    val completedGames: Set<GameType> = emptySet(),
    /** 그날 게임별 최고 점수의 합 */
    val totalScore: Int = 0,
    /** 불꽃 방패로 연속 기록을 지킨 날 */
    val shielded: Boolean = false,
) {
    val isCompleted: Boolean get() = completedGames.size == GameType.entries.size

    companion object {
        fun completedGamesOf(mask: Int): Set<GameType> =
            GameType.entries.filterTo(mutableSetOf()) { mask and it.bit != 0 }
    }
}
