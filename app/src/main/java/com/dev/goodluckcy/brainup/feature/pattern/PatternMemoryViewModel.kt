package com.dev.goodluckcy.brainup.feature.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 중단 정책: 패턴 재생 중 앱이 백그라운드로 가면 재생을 멈추고,
 * 복귀 시 같은 패턴을 처음부터 다시 보여준다. 입력 단계는 시간 제한이 없으므로 그대로 유지한다.
 */
@HiltViewModel
class PatternMemoryViewModel @Inject constructor(
    private val engine: PatternMemoryEngine,
    private val clock: MonotonicClock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternMemoryUiState())
    val uiState: StateFlow<PatternMemoryUiState> = _uiState.asStateFlow()

    private var phaseJob: Job? = null
    private var flashJob: Job? = null
    private var startedAtMs = 0L
    private var isStopped = false
    private var pendingPlayback = false

    fun start() {
        if (_uiState.value.phase != GamePhase.Ready) return
        startedAtMs = clock.elapsedRealtimeMs()
        startRound(round = 1, sequence = engine.initialSequence())
    }

    fun onTileTap(tile: Int) {
        require(tile in 0 until PatternMemoryEngine.TILE_COUNT) { "tile out of range: $tile" }
        val state = _uiState.value
        if (!state.isInputEnabled) return

        if (!engine.isCorrectTap(state.sequence, state.inputCount, tile)) {
            phaseJob?.cancel()
            flashJob?.cancel()
            _uiState.update {
                it.copy(
                    phase = GamePhase.Finished,
                    flashedTile = null,
                    wrongTile = tile,
                    expectedTile = it.sequence[it.inputCount],
                    durationMs = clock.elapsedRealtimeMs() - startedAtMs,
                )
            }
            return
        }

        flash(tile)
        val inputCount = state.inputCount + 1
        if (inputCount < state.sequence.size) {
            _uiState.update { it.copy(inputCount = inputCount) }
            return
        }
        _uiState.update {
            it.copy(
                phase = GamePhase.Success,
                inputCount = inputCount,
                score = it.score + engine.scoreForRound(it.sequence.size),
                roundsCleared = it.round,
            )
        }
        phaseJob = viewModelScope.launch {
            delay(SUCCESS_FEEDBACK_MS)
            startRound(round = state.round + 1, sequence = engine.extend(state.sequence))
        }
    }

    fun onStop() {
        isStopped = true
        if (_uiState.value.phase == GamePhase.Memorizing) {
            phaseJob?.cancel()
            _uiState.update { it.copy(litTile = null) }
            pendingPlayback = true
        }
    }

    fun onStart() {
        isStopped = false
        if (pendingPlayback) {
            pendingPlayback = false
            runPlayback()
        }
    }

    fun result(): GameResult {
        val state = _uiState.value
        check(state.phase == GamePhase.Finished) { "Game is not finished" }
        return GameResult(
            gameType = GameType.PATTERN,
            score = state.score,
            level = longestClearedLength(state.roundsCleared),
            roundsCleared = state.roundsCleared,
            durationMs = state.durationMs,
        )
    }

    private fun longestClearedLength(roundsCleared: Int): Int =
        if (roundsCleared == 0) 0 else PatternMemoryEngine.START_LENGTH + roundsCleared - 1

    private fun startRound(round: Int, sequence: List<Int>) {
        _uiState.update {
            it.copy(
                phase = GamePhase.Memorizing,
                round = round,
                sequence = sequence,
                inputCount = 0,
                litTile = null,
                flashedTile = null,
            )
        }
        runPlayback()
    }

    private fun runPlayback() {
        phaseJob?.cancel()
        if (isStopped) {
            pendingPlayback = true
            return
        }
        val sequence = _uiState.value.sequence
        val litMs = engine.litDurationMs(sequence.size)
        phaseJob = viewModelScope.launch {
            delay(PLAYBACK_START_DELAY_MS)
            for (tile in sequence) {
                _uiState.update { it.copy(litTile = tile) }
                delay(litMs)
                _uiState.update { it.copy(litTile = null) }
                delay(PatternMemoryEngine.GAP_MS)
            }
            _uiState.update { it.copy(phase = GamePhase.Answering) }
        }
    }

    private fun flash(tile: Int) {
        flashJob?.cancel()
        _uiState.update { it.copy(flashedTile = tile) }
        flashJob = viewModelScope.launch {
            delay(TAP_FLASH_MS)
            _uiState.update { it.copy(flashedTile = null) }
        }
    }

    companion object {
        const val PLAYBACK_START_DELAY_MS = 600L
        const val SUCCESS_FEEDBACK_MS = 800L
        const val TAP_FLASH_MS = 180L
    }
}
