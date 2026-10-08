package com.dev.goodluckcy.brainup.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsLogger @Inject constructor(
    @ApplicationContext context: Context,
) : AnalyticsLogger {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun log(event: AnalyticsEvent) {
        val bundle = Bundle()
        event.params.forEach { (key, value) ->
            when (value) {
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(event.name, bundle)
    }
}
