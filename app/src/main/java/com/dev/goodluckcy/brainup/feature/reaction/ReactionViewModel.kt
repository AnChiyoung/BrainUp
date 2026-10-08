package com.dev.goodluckcy.brainup.feature.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 중단 정책: 대기/터치 단계에서 앱이 백그라운드로 가면 진행 중인 시도를 취소하고
 * Ready로 되돌린다. 이미 기록된 시도는 유지한다.
 */
@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val engine: ReactionEngine,
    private val clock: MonotonicClock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState: StateFlow<ReactionUiState> = _uiState.asStateFlow()

    private var waitJob: Job? = null
    private var goAtNanos = 0L
    private var startedAtMs: Long? = null

    /** 화면 터치. 터치 다운 시점에 호출해야 측정 지연이 줄어든다. */
    fun onTap() {
        when (_uiState.value.phase) {
            ReactionPhase.Ready,
            ReactionPhase.TooEarly,
            ReactionPhase.AttemptResult -> startWaiting()
            ReactionPhase.Waiting -> onFalseStart()
            ReactionPhase.Go -> onReaction(clock.elapsedRealtimeNanos())
            ReactionPhase.Finished -> Unit
        }
    }

    fun onStop() {
        when (_uiState.value.phase) {
            ReactionPhase.Waiting, ReactionPhase.Go -> {
                waitJob?.cancel()
                _uiState.update { it.copy(phase = ReactionPhase.Ready) }
            }
            else -> Unit
        }
    }

    fun result(): GameResult {
        val state = _uiState.value
        check(state.phase == ReactionPhase.Finished) { "Game is not finished" }
        return GameResult(
            gameType = GameType.REACTION,
            score = state.score,
            level = 1,
            roundsCleared = state.completedAttempts,
            durationMs = state.durationMs,
            medianReactionMs = state.medianMs,
            bestReactionMs = state.bestMs,
        )
    }

    private fun startWaiting() {
        if (startedAtMs == null) startedAtMs = clock.elapsedRealtimeMs()
        _uiState.update { it.copy(phase = ReactionPhase.Waiting) }
        waitJob?.cancel()
        waitJob = viewModelScope.launch {
            delay(engine.randomDelayMs())
            goAtNanos = clock.elapsedRealtimeNanos()
            _uiState.update { it.copy(phase = ReactionPhase.Go) }
        }
    }

    private fun onFalseStart() {
        waitJob?.cancel()
        _uiState.update { it.copy(phase = ReactionPhase.TooEarly, falseStarts = it.falseStarts + 1) }
    }

    private fun onReaction(tappedAtNanos: Long) {
        val reactionMs = (tappedAtNanos - goAtNanos) / NANOS_PER_MS
        val reactions = _uiState.value.reactionsMs + reactionMs
        if (reactions.size < ReactionEngine.ATTEMPTS) {
            _uiState.update { it.copy(phase = ReactionPhase.AttemptResult, reactionsMs = reactions) }
            return
        }
        val median = engine.median(reactions)
        _uiState.update {
            it.copy(
                phase = ReactionPhase.Finished,
                reactionsMs = reactions,
                medianMs = median,
                bestMs = engine.best(reactions),
                score = engine.score(median),
                durationMs = clock.elapsedRealtimeMs() - (startedAtMs ?: 0L),
            )
        }
    }

    private companion object {
        const val NANOS_PER_MS = 1_000_000L
    }
}
