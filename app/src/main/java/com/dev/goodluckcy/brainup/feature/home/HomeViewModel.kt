package com.dev.goodluckcy.brainup.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.domain.repository.DailyChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    dailyChallengeRepository: DailyChallengeRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        dailyChallengeRepository.observeToday(),
        dailyChallengeRepository.observeStreak(),
    ) { today, streak ->
        HomeUiState(
            completedGames = today.completedGames,
            todayScore = today.totalScore,
            streakDays = streak,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
