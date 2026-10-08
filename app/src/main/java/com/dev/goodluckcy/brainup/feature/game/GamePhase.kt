package com.dev.goodluckcy.brainup.feature.game

/** 미니게임 공통 진행 상태. */
sealed interface GamePhase {
    /** 시작 전 안내 */
    data object Ready : GamePhase

    /** 문제 표시(암기) 중 */
    data object Memorizing : GamePhase

    /** 사용자 입력 중 */
    data object Answering : GamePhase

    /** 라운드 성공 피드백 표시 중 */
    data object Success : GamePhase

    /** 게임 종료 */
    data object Finished : GamePhase
}
