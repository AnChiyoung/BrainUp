package com.dev.goodluckcy.brainup.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dev.goodluckcy.brainup.core.common.di.ApplicationScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UMP 동의 확인 후 Mobile Ads SDK를 초기화한다.
 * 동의가 필요한 지역(EEA·영국 등)에서만 동의 양식이 표시된다.
 */
@Singleton
class AdsConsentManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val mobileAdsInitializing = AtomicBoolean(false)

    private val _isAdsReady = MutableStateFlow(false)
    /** 광고 요청이 허용되고 SDK 초기화가 끝났으면 true */
    val isAdsReady: StateFlow<Boolean> = _isAdsReady.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    /** 설정 화면에 '광고 개인정보 설정' 진입점을 보여줘야 하면 true */
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    /** 앱 시작 시 매번 호출한다. */
    fun gatherConsent(activity: Activity) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Log.w(TAG, "Consent form error: ${it.message}") }
                    onConsentUpdated()
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.message}")
                onConsentUpdated()
            },
        )
        // 이전 세션에서 이미 동의를 받았다면 업데이트 결과를 기다리지 않고 광고를 준비한다.
        if (consentInformation.canRequestAds()) initializeMobileAds()
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let { Log.w(TAG, "Privacy options form error: ${it.message}") }
            onConsentUpdated()
        }
    }

    private fun onConsentUpdated() {
        _isPrivacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (consentInformation.canRequestAds()) initializeMobileAds()
    }

    private fun initializeMobileAds() {
        if (!mobileAdsInitializing.compareAndSet(false, true)) return
        applicationScope.launch(Dispatchers.IO) {
            MobileAds.initialize(context) { _isAdsReady.value = true }
        }
    }

    private companion object {
        const val TAG = "AdsConsent"
    }
}
