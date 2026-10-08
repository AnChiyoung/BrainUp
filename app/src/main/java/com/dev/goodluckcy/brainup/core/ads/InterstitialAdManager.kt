package com.dev.goodluckcy.brainup.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.core.analytics.AdFormat
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.di.ApplicationScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** 결과 확인 후 화면 전환 시점에만 빈도 제한을 지켜 전면 광고를 보여준다. */
@Singleton
class InterstitialAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val consentManager: AdsConsentManager,
    private val frequencyStore: AdFrequencyStore,
    private val analytics: AnalyticsLogger,
    private val clock: Clock,
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    init {
        applicationScope.launch(Dispatchers.Main) {
            consentManager.isAdsReady.first { it }
            load()
        }
    }

    /**
     * 조건을 만족하면 전면 광고를 보여주고 닫힌 뒤 [onDone]을 호출한다.
     * 조건을 만족하지 않거나 실패하면 바로 [onDone]을 호출한다.
     */
    fun showIfEligible(activity: Activity, onDone: () -> Unit) {
        applicationScope.launch(Dispatchers.Main) {
            val ad = interstitialAd
            val snapshot = frequencyStore.snapshot()
            val canShow = InterstitialPolicy.canShow(
                isAdLoaded = ad != null,
                gamesSinceLastAd = snapshot.gamesSinceLastAd,
                lastShownAtMs = snapshot.lastShownAtMs,
                nowMs = clock.millis(),
            )
            if (ad == null || !canShow || activity.isFinishing) {
                onDone()
                return@launch
            }
            interstitialAd = null
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    applicationScope.launch { frequencyStore.onInterstitialShown(clock.millis()) }
                }

                override fun onAdImpression() {
                    analytics.log(AnalyticsEvent.adImpression(AdFormat.INTERSTITIAL))
                }

                override fun onAdDismissedFullScreenContent() {
                    onDone()
                    load()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Interstitial failed to show: ${error.message}")
                    onDone()
                    load()
                }
            }
            ad.show(activity)
        }
    }

    private fun load() {
        if (interstitialAd != null || isLoading || !consentManager.isAdsReady.value) return
        isLoading = true
        InterstitialAd.load(
            context,
            BuildConfig.AD_UNIT_INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                    isLoading = false
                }
            },
        )
    }

    private companion object {
        const val TAG = "InterstitialAd"
    }
}
