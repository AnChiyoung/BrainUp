package com.dev.goodluckcy.brainup.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.domain.model.BestRecord
import com.dev.goodluckcy.brainup.domain.model.DailyProgress
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.repository.DailyChallengeRepository
import com.dev.goodluckcy.brainup.domain.repository.GameRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val streakDays: Int = 0,
    val bestRecords: Map<GameType, BestRecord> = emptyMap(),
    /** 오래된 날짜부터, 마지막이 오늘 */
    val recentDays: List<DailyProgress> = emptyList(),
) {
    val isTodayCompleted: Boolean get() = recentDays.lastOrNull()?.isCompleted == true
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    dailyChallengeRepository: DailyChallengeRepository,
    gameRecordRepository: GameRecordRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        dailyChallengeRepository.observeStreak(),
        gameRecordRepository.observeBestRecords(),
        dailyChallengeRepository.observeRecentDays(RECENT_DAYS),
    ) { streak, bestRecords, recentDays ->
        StatsUiState(
            isLoading = false,
            streakDays = streak,
            bestRecords = bestRecords.associateBy { it.gameType },
            recentDays = recentDays,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    companion object {
        const val RECENT_DAYS = 7
    }
}
