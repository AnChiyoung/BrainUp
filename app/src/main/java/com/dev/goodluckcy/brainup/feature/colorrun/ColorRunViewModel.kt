package com.dev.goodluckcy.brainup.feature.colorrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.MonotonicClock
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ColorRunPhase {
    /** 시작 전 */
    Ready,

    /** 달리는 중 */
    Running,

    /** 백그라운드에서 돌아와 멈춘 상태 */
    Paused,

    /** 결승선 통과 */
    Finished,
}

data class ColorRunUiState(
    val phase: ColorRunPhase = ColorRunPhase.Ready,
    val run: ColorRunState? = null,
    val score: Int = 0,
)

/**
 * 중단 정책: 달리는 중 앱이 백그라운드로 가면 멈추고(Paused), 탭하면 이어서 달린다.
 * 멈춘 시간은 기록에 포함하지 않는다.
 */
@HiltViewModel
class ColorRunViewModel @Inject constructor(
    private val engine: ColorRunEngine,
    private val clock: MonotonicClock,
    private val analytics: AnalyticsLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorRunUiState())
    val uiState: StateFlow<ColorRunUiState> = _uiState.asStateFlow()

    private var loopJob: Job? = null

    fun start() {
        if (_uiState.value.phase != ColorRunPhase.Ready) return
        analytics.log(AnalyticsEvent.gameStart(GameType.COLOR_RUN))
        _uiState.update { it.copy(phase = ColorRunPhase.Running, run = engine.newRun()) }
        startLoop()
    }

    fun resume() {
        if (_uiState.value.phase != ColorRunPhase.Paused) return
        _uiState.update { it.copy(phase = ColorRunPhase.Running) }
        startLoop()
    }

    fun onColorTap(color: Int) {
        val state = _uiState.value
        if (state.phase != ColorRunPhase.Running) return
        val run = state.run ?: return
        _uiState.update { it.copy(run = engine.tap(run, color)) }
    }

    fun onStop() {
        if (_uiState.value.phase != ColorRunPhase.Running) return
        loopJob?.cancel()
        _uiState.update { it.copy(phase = ColorRunPhase.Paused) }
    }

    fun result(): GameResult {
        val state = _uiState.value
        val run = checkNotNull(state.run) { "Game is not started" }
        check(state.phase == ColorRunPhase.Finished) { "Game is not finished" }
        return GameResult(
            gameType = GameType.COLOR_RUN,
            score = state.score,
            level = run.maxCombo,
            roundsCleared = run.cleanGates,
            durationMs = run.elapsedMs,
        )
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            var last = clock.elapsedRealtimeMs()
            while (isActive) {
                delay(FRAME_MS)
                val now = clock.elapsedRealtimeMs()
                val run = _uiState.value.run ?: return@launch
                val next = engine.tick(run, now - last)
                last = now
                if (next.isFinished) {
                    _uiState.update {
                        it.copy(phase = ColorRunPhase.Finished, run = next, score = engine.score(next.elapsedMs))
                    }
                    return@launch
                }
                _uiState.update { it.copy(run = next) }
            }
        }
    }

    private companion object {
        const val FRAME_MS = 16L
    }
}
