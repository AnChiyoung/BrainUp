package com.dev.goodluckcy.brainup.core.ads

/**
 * 전면 광고 노출 빈도 제한 (자체 정책, 설계서 7장)
 * - 마지막 노출 이후 게임을 [MIN_GAMES_BETWEEN_ADS]판 이상 완료했고
 * - 마지막 노출 후 [MIN_INTERVAL_MS] 이상 지났을 때만 노출한다.
 */
object InterstitialPolicy {
    const val MIN_GAMES_BETWEEN_ADS = 3
    const val MIN_INTERVAL_MS = 5 * 60 * 1000L

    fun canShow(
        isAdLoaded: Boolean,
        gamesSinceLastAd: Int,
        lastShownAtMs: Long?,
        nowMs: Long,
    ): Boolean {
        if (!isAdLoaded || gamesSinceLastAd < MIN_GAMES_BETWEEN_ADS) return false
        if (lastShownAtMs == null) return true
        // 기기 시간이 뒤로 바뀐 경우 영구히 막히지 않도록 허용한다.
        return nowMs < lastShownAtMs || nowMs - lastShownAtMs >= MIN_INTERVAL_MS
    }
}
