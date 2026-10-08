package com.dev.goodluckcy.brainup.feature.home

import com.dev.goodluckcy.brainup.domain.model.GameType

data class HomeUiState(
    val completedGames: Set<GameType> = emptySet(),
    val todayScore: Int = 0,
    val streakDays: Int = 0,
) {
    val completedCount: Int get() = completedGames.size
    val totalCount: Int get() = GameType.entries.size
    val isDailyCompleted: Boolean get() = completedCount == totalCount
}
