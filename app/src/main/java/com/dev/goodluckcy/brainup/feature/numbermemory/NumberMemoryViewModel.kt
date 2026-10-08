package com.dev.goodluckcy.brainup.feature.numbermemory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.MonotonicClock
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.feature.game.GamePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 중단 정책: 암기 중 앱이 백그라운드로 가면 타이머를 멈추고,
 * 복귀 시 같은 숫자를 처음부터 다시 보여준다. 입력 단계는 시간 제한이 없으므로 그대로 유지한다.
 */
@HiltViewModel
class NumberMemoryViewModel @Inject constructor(
    private val engine: NumberMemoryEngine,
    private val clock: MonotonicClock,
    private val analytics: AnalyticsLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumberMemoryUiState())
    val uiState: StateFlow<NumberMemoryUiState> = _uiState.asStateFlow()

    private var phaseJob: Job? = null
    private var startedAtMs = 0L
    private var isStopped = false
    private var pendingMemorize = false

    fun start() {
        if (_uiState.value.phase != GamePhase.Ready) return
        startedAtMs = clock.elapsedRealtimeMs()
        analytics.log(AnalyticsEvent.gameStart(GameType.NUMBER_MEMORY))
        startRound(round = 1)
    }

    /** 보상형 광고 시청 후 같은 길이의 새 숫자로 현재 라운드를 다시 진행한다. */
    fun continueAfterReward() {
        val state = _uiState.value
        if (!state.canContinue) return
        _uiState.update { it.copy(continueUsed = true) }
        startRound(state.round)
    }

    fun onDigit(digit: Int) {
        require(digit in 0..9) { "digit must be 0..9: $digit" }
        val state = _uiState.value
        if (!state.isInputEnabled || state.input.size >= state.sequence.size) return
        val input = state.input + digit
        _uiState.update { it.copy(input = input) }
        if (input.size == state.sequence.size) submit(input)
    }

    fun onDelete() {
        val state = _uiState.value
        if (!state.isInputEnabled || state.input.isEmpty()) return
        _uiState.update { it.copy(input = it.input.dropLast(1)) }
    }

    fun onStop() {
        isStopped = true
        if (_uiState.value.phase == GamePhase.Memorizing) {
            phaseJob?.cancel()
            pendingMemorize = true
        }
    }

    fun onStart() {
        isStopped = false
        if (pendingMemorize) {
            pendingMemorize = false
            runMemorizeTimer()
        }
    }

    fun result(): GameResult {
        val state = _uiState.value
        check(state.phase == GamePhase.Finished) { "Game is not finished" }
        return GameResult(
            gameType = GameType.NUMBER_MEMORY,
            score = state.score,
            level = state.level,
            roundsCleared = state.roundsCleared,
            durationMs = state.durationMs,
        )
    }

    private fun startRound(round: Int) {
        val length = engine.lengthForRound(round)
        _uiState.update {
            it.copy(
                phase = GamePhase.Memorizing,
                round = round,
                level = engine.levelForRound(round),
                sequence = engine.generateSequence(length),
                input = emptyList(),
                memorizeDurationMs = engine.memorizeDurationMs(length),
            )
        }
        runMemorizeTimer()
    }

    private fun runMemorizeTimer() {
        phaseJob?.cancel()
        if (isStopped) {
            pendingMemorize = true
            return
        }
        _uiState.update { it.copy(memorizeToken = it.memorizeToken + 1) }
        val durationMs = _uiState.value.memorizeDurationMs
        phaseJob = viewModelScope.launch {
            delay(durationMs)
            _uiState.update { it.copy(phase = GamePhase.Answering) }
        }
    }

    private fun submit(input: List<Int>) {
        val state = _uiState.value
        if (engine.isCorrect(state.sequence, input)) {
            _uiState.update {
                it.copy(
                    phase = GamePhase.Success,
                    score = it.score + engine.scoreForRound(it.sequence.size),
                    roundsCleared = it.round,
                )
            }
            phaseJob = viewModelScope.launch {
                delay(SUCCESS_FEEDBACK_MS)
                startRound(state.round + 1)
            }
        } else {
            phaseJob?.cancel()
            _uiState.update {
                it.copy(
                    phase = GamePhase.Finished,
                    durationMs = clock.elapsedRealtimeMs() - startedAtMs,
                )
            }
        }
    }

    companion object {
        const val SUCCESS_FEEDBACK_MS = 800L
    }
}
