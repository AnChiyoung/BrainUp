package com.dev.goodluckcy.brainup.core.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.core.analytics.AdFormat
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** 화면 하단 고정형 적응형 배너. 광고 준비 전에는 공간을 차지하지 않는다. */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val adServices = LocalAdServices.current ?: return
    val isAdsReady by adServices.consent.isAdsReady.collectAsStateWithLifecycle()
    if (!isAdsReady) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val widthDp = maxWidth.value.toInt()
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        val adView = remember(adSize) {
            AdView(context).apply {
                adUnitId = BuildConfig.AD_UNIT_BANNER
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdImpression() {
                        adServices.analytics.log(AnalyticsEvent.adImpression(AdFormat.BANNER))
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }
        LifecycleResumeEffect(adView) {
            adView.resume()
            onPauseOrDispose { adView.pause() }
        }
        AndroidView(
            factory = { adView },
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
        )
    }
}
