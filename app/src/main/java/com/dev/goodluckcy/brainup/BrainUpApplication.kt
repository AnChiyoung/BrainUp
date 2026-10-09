package com.dev.goodluckcy.brainup

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BrainUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 개발 중 크래시가 실제 통계에 섞이지 않도록 디버그 빌드에서는 수집하지 않는다.
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }
}
