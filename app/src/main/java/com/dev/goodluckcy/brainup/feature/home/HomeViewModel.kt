package com.dev.goodluckcy.brainup.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.repository.CoinRepository
import com.dev.goodluckcy.brainup.domain.repository.DailyChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    dailyChallengeRepository: DailyChallengeRepository,
    private val coinRepository: CoinRepository,
) : ViewModel() {

    private val dialogs = MutableStateFlow<List<HomeDialog>>(emptyList())
    private var claiming = false

    private val progress = combine(
        dailyChallengeRepository.observeToday(),
        dailyChallengeRepository.observeStreak(),
        coinRepository.observeChest(),
    ) { today, streak, chest ->
        HomeUiState(
            completedGames = today.completedGames,
            todayScore = today.totalScore,
            streakDays = streak,
            chestClaimed = chest.claimed,
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        progress,
        coinRepository.observeBalance(),
        coinRepository.observeInventory(),
        dialogs,
    ) { state, balance, inventory, queue ->
        state.copy(
            coins = balance,
            shields = inventory.shields,
            brainy = inventory.equippedIn(ItemSlot.BRAINY_COLOR),
            mapTheme = inventory.equippedIn(ItemSlot.MAP_THEME),
            dialog = queue.firstOrNull(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            coinRepository.grantWelcomeIfNeeded()?.let { show(HomeDialog.Welcome(it)) }
            // 어제를 놓쳤다면 지도를 여는 순간 방패로 연속 기록을 지킨다.
            coinRepository.applyStreakShields()?.let { show(HomeDialog.ShieldUsed(it)) }
        }
    }

    fun openChest() {
        if (uiState.value.isChestReady) show(HomeDialog.Chest)
    }

    /** [double]이면 보상형 광고를 끝까지 본 경우다. */
    fun claimChest(double: Boolean) {
        if (claiming) return
        claiming = true
        viewModelScope.launch {
            try {
                coinRepository.claimChest(double)
            } finally {
                claiming = false
                dismiss(HomeDialog.Chest)
            }
        }
    }

    fun dismissDialog() {
        dialogs.update { it.drop(1) }
    }

    private fun show(dialog: HomeDialog) {
        dialogs.update { if (dialog in it) it else it + dialog }
    }

    private fun dismiss(dialog: HomeDialog) {
        dialogs.update { it - dialog }
    }
}
