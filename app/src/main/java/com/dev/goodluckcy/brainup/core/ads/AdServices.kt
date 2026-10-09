package com.dev.goodluckcy.brainup.core.ads

import androidx.compose.runtime.staticCompositionLocalOf
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import javax.inject.Inject
import javax.inject.Singleton

/** 화면에서 광고 기능에 접근하기 위한 묶음. MainActivity에서 [LocalAdServices]로 제공한다. */
@Singleton
class AdServices @Inject constructor(
    val consent: AdsConsentManager,
    val interstitial: InterstitialAdManager,
    val rewarded: RewardedAdManager,
    val analytics: AnalyticsLogger,
)

/** Preview 등 제공되지 않은 환경에서는 null이며, 광고 UI를 그리지 않는다. */
val LocalAdServices = staticCompositionLocalOf<AdServices?> { null }
