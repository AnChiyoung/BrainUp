package com.dev.goodluckcy.brainup.core.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.analytics.AdFormat
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Brainy
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * 화면 하단 고정형 적응형 배너.
 * 광고 높이만큼 자리를 처음부터 잡아 두고, 광고가 오기 전(또는 받을 수 없을 때)에는
 * 같은 자리에 안내판을 보여준다. 광고가 로드돼도 레이아웃이 움직이지 않는다.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val adServices = LocalAdServices.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val widthDp = maxWidth.value.toInt()
        // Preview 등 광고 서비스가 없는 환경에서는 일반 배너 높이로 자리를 잡는다.
        val adSize = remember(widthDp, adServices) {
            if (adServices != null) AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp) else null
        }
        val slotHeight = adSize?.height?.dp ?: FALLBACK_HEIGHT
        var isLoaded by remember(adSize) { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(slotHeight),
        ) {
            if (adServices != null && adSize != null) {
                val isAdsReady by adServices.consent.isAdsReady.collectAsStateWithLifecycle()
                if (isAdsReady) {
                    BannerAdView(
                        adServices = adServices,
                        adSize = adSize,
                        onLoaded = { isLoaded = true },
                    )
                }
            }
            if (!isLoaded) {
                AdLoadingPlaceholder(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun BannerAdView(
    adServices: AdServices,
    adSize: AdSize,
    onLoaded: () -> Unit,
) {
    val context = LocalContext.current
    var failureCount by remember(adSize) { mutableIntStateOf(0) }
    val adView = remember(adSize) {
        AdView(context).apply {
            adUnitId = BuildConfig.AD_UNIT_BANNER
            setAdSize(adSize)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    onLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    failureCount++
                }

                override fun onAdImpression() {
                    adServices.analytics.log(AnalyticsEvent.adImpression(AdFormat.BANNER))
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    LaunchedEffect(adView, failureCount) {
        if (failureCount > 0) {
            delay(RETRY_DELAY_MS)
            adView.loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
    AndroidView(factory = { adView }, modifier = Modifier.fillMaxSize())
}

/** 광고가 오기 전 같은 자리를 채우는 안내판 */
@Composable
private fun AdLoadingPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(NightDeep, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Brainy, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.size(22.dp))
        }
        Text(
            text = stringResource(R.string.ad_loading),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.ad_label),
            style = MaterialTheme.typography.labelSmall,
            color = Lavender,
        )
    }
}

private val FALLBACK_HEIGHT: Dp = 60.dp
private const val RETRY_DELAY_MS = 60_000L

@Preview(widthDp = 390)
@Composable
private fun BannerAdPlaceholderPreview() {
    BrainUpTheme {
        BannerAd()
    }
}
