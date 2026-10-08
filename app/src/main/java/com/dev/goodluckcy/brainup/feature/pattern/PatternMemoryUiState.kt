package com.dev.goodluckcy.brainup.feature.pattern

import com.dev.goodluckcy.brainup.feature.game.GamePhase

data class PatternMemoryUiState(
    val phase: GamePhase = GamePhase.Ready,
    val round: Int = 1,
    val sequence: List<Int> = emptyList(),
    /** 패턴 재생 중 현재 켜진 타일 */
    val litTile: Int? = null,
    /** 사용자가 방금 터치한 타일(짧게 강조) */
    val flashedTile: Int? = null,
    val inputCount: Int = 0,
    val score: Int = 0,
    val roundsCleared: Int = 0,
    /** 종료 시 사용자가 잘못 누른 타일 */
    val wrongTile: Int? = null,
    /** 종료 시 눌러야 했던 타일 */
    val expectedTile: Int? = null,
    val durationMs: Long = 0L,
) {
    val isInputEnabled: Boolean get() = phase == GamePhase.Answering
}
