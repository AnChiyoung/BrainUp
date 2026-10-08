package com.dev.goodluckcy.brainup.feature.numbermemory

import com.dev.goodluckcy.brainup.feature.game.GamePhase

data class NumberMemoryUiState(
    val phase: GamePhase = GamePhase.Ready,
    val round: Int = 1,
    val level: Int = 1,
    val sequence: List<Int> = emptyList(),
    val input: List<Int> = emptyList(),
    val score: Int = 0,
    val roundsCleared: Int = 0,
    val memorizeDurationMs: Long = 0L,
    /** 암기 타이머가 (재)시작될 때마다 증가한다. UI 카운트다운 애니메이션의 key로 사용한다. */
    val memorizeToken: Int = 0,
    val durationMs: Long = 0L,
) {
    val isInputEnabled: Boolean get() = phase == GamePhase.Answering
}
