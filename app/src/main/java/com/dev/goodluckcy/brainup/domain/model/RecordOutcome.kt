package com.dev.goodluckcy.brainup.domain.model

/** 게임 결과 저장 후 결과 화면에 알려줄 정보 */
data class RecordOutcome(
    /** 첫 기록이거나 이전 최고 점수보다 높으면 true */
    val isPersonalBest: Boolean,
    val previousBestScore: Int?,
    /** 이번 기록으로 오늘의 도전이 처음 완료되었으면 true */
    val dailyCompletedNow: Boolean,
    val todayCompletedCount: Int,
)
