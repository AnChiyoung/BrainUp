package com.dev.goodluckcy.brainup.feature.result

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dev.goodluckcy.brainup.core.ads.AdFrequencyStore
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.di.ApplicationScope
import com.dev.goodluckcy.brainup.core.navigation.ResultRoute
import com.dev.goodluckcy.brainup.domain.model.CoinReward
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.RecordOutcome
import com.dev.goodluckcy.brainup.domain.repository.CoinRepository
import com.dev.goodluckcy.brainup.domain.repository.GameRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val result: GameResult,
    /** 저장이 끝나기 전에는 null */
    val outcome: RecordOutcome? = null,
    /** 코인 지급이 끝나기 전에는 null */
    val coinReward: CoinReward? = null,
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: GameRecordRepository,
    coinRepository: CoinRepository,
    analytics: AnalyticsLogger,
    adFrequencyStore: AdFrequencyStore,
    @ApplicationScope applicationScope: CoroutineScope,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ResultUiState(result = savedStateHandle.toRoute<ResultRoute>().toGameResult()),
    )
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        // 화면 회전·프로세스 재생성 시 중복 저장하지 않는다.
        if (savedStateHandle.get<Boolean>(KEY_SAVED) != true) {
            savedStateHandle[KEY_SAVED] = true
            val result = _uiState.value.result
            analytics.log(AnalyticsEvent.gameComplete(result.gameType, result.score))
            // 결과 화면을 바로 떠나도 저장이 취소되지 않도록 앱 스코프에서 실행한다.
            val saving = applicationScope.async {
                adFrequencyStore.onGameCompleted()
                runCatching {
                    val outcome = repository.record(result)
                    if (outcome.dailyCompletedNow) {
                        analytics.log(AnalyticsEvent.dailyComplete(outcome.todayTotalScore))
                    }
                    // 기록 저장 결과(최고 기록·오늘의 도전 완료)에 따라 코인을 지급한다.
                    val coinReward = coinRepository.rewardGame(
                        isPersonalBest = outcome.isPersonalBest,
                        dailyCompletedNow = outcome.dailyCompletedNow,
                    )
                    outcome to coinReward
                }
                    .onFailure { Log.e(TAG, "Failed to save game result", it) }
                    .getOrNull()
            }
            viewModelScope.launch {
                saving.await()?.let { (outcome, coinReward) ->
                    _uiState.update { it.copy(outcome = outcome, coinReward = coinReward) }
                }
            }
        }
    }

    private companion object {
        const val TAG = "ResultViewModel"
        const val KEY_SAVED = "result_saved"
    }
}
