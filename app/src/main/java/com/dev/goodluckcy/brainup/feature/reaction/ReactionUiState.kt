package com.dev.goodluckcy.brainup.feature.reaction

/**
 * 반응 속도 게임 전용 진행 상태.
 * 암기/입력 단계가 없어 공통 GamePhase 대신 사용한다.
 */
sealed interface ReactionPhase {
    /** 시도 시작 전(최초 시작, 백그라운드 복귀 후 재개 포함) */
    data object Ready : ReactionPhase

    /** 색 변경 대기 중 */
    data object Waiting : ReactionPhase

    /** 색이 바뀜 — 지금 터치 */
    data object Go : ReactionPhase

    /** 색 변경 전 터치(무효) */
    data object TooEarly : ReactionPhase

    /** 한 번의 유효 시도 결과 표시 */
    data object AttemptResult : ReactionPhase

    /** 모든 시도 완료 */
    data object Finished : ReactionPhase
}

data class ReactionUiState(
    val phase: ReactionPhase = ReactionPhase.Ready,
    val reactionsMs: List<Long> = emptyList(),
    val falseStarts: Int = 0,
    val medianMs: Long? = null,
    val bestMs: Long? = null,
    val score: Int = 0,
    val durationMs: Long = 0L,
) {
    val totalAttempts: Int get() = ReactionEngine.ATTEMPTS
    val completedAttempts: Int get() = reactionsMs.size
    val lastReactionMs: Long? get() = reactionsMs.lastOrNull()
}
