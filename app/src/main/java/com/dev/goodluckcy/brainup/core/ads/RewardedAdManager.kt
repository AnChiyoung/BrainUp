package com.dev.goodluckcy.brainup.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.core.analytics.AdFormat
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.analytics.RewardType
import com.dev.goodluckcy.brainup.core.common.di.ApplicationScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** 사용자가 직접 선택했을 때만 보여주는 보상형 광고 */
@Singleton
class RewardedAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val consentManager: AdsConsentManager,
    private val analytics: AnalyticsLogger,
) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        applicationScope.launch(Dispatchers.Main) {
            consentManager.isAdsReady.first { it }
            load()
        }
    }

    /**
     * 광고를 보여주고, 사용자가 보상을 받은 경우 광고가 닫힌 뒤 [onRewarded]를 호출한다.
     * 광고 위에서 게임이 진행되지 않도록 보상 적용은 닫힌 뒤에 한다.
     */
    fun show(activity: Activity, rewardType: RewardType, onRewarded: () -> Unit) {
        val ad = rewardedAd ?: return
        rewardedAd = null
        _isLoaded.value = false
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                analytics.log(AnalyticsEvent.adImpression(AdFormat.REWARDED))
            }

            override fun onAdDismissedFullScreenContent() {
                if (earned) {
                    analytics.log(AnalyticsEvent.rewardGranted(rewardType))
                    onRewarded()
                }
                load()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded failed to show: ${error.message}")
                load()
            }
        }
        ad.show(activity) { earned = true }
    }

    private fun load() {
        if (rewardedAd != null || isLoading || !consentManager.isAdsReady.value) return
        isLoading = true
        RewardedAd.load(
            context,
            BuildConfig.AD_UNIT_REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    _isLoaded.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded failed to load: ${error.message}")
                    isLoading = false
                }
            },
        )
    }

    private companion object {
        const val TAG = "RewardedAd"
    }
}
