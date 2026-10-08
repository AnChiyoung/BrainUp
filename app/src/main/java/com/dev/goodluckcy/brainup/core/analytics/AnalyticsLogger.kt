package com.dev.goodluckcy.brainup.core.analytics

import com.dev.goodluckcy.brainup.domain.model.GameType

interface AnalyticsLogger {
    fun log(event: AnalyticsEvent)
}

/** Android/iOS 공통 Analytics 이벤트 명세 (설계서 8장) */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
) {
    companion object {
        fun gameStart(gameType: GameType) =
            AnalyticsEvent("game_start", mapOf("game_type" to gameType.analyticsName))

        fun gameComplete(gameType: GameType, score: Int) =
            AnalyticsEvent("game_complete", mapOf("game_type" to gameType.analyticsName, "score" to score))

        fun dailyComplete(totalScore: Int) =
            AnalyticsEvent("daily_complete", mapOf("total_score" to totalScore))

        fun adImpression(format: AdFormat) =
            AnalyticsEvent("ad_impression", mapOf("ad_format" to format.analyticsName))

        fun rewardGranted(rewardType: RewardType) =
            AnalyticsEvent("reward_granted", mapOf("reward_type" to rewardType.analyticsName))
    }
}

enum class AdFormat(val analyticsName: String) {
    BANNER("banner"),
    INTERSTITIAL("interstitial"),
    REWARDED("rewarded"),
}

enum class RewardType(val analyticsName: String) {
    /** 오답 후 같은 라운드 한 번 더 */
    CONTINUE("continue"),
}
