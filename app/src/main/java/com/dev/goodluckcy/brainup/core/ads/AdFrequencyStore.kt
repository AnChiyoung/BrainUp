package com.dev.goodluckcy.brainup.core.ads

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 전면 광고 빈도 제한용 로컬 카운터 (DataStore) */
@Singleton
class AdFrequencyStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    data class Snapshot(val gamesSinceLastAd: Int, val lastShownAtMs: Long?)

    suspend fun snapshot(): Snapshot = dataStore.data.map { prefs ->
        Snapshot(
            gamesSinceLastAd = prefs[GAMES_SINCE_LAST_AD] ?: 0,
            lastShownAtMs = prefs[LAST_SHOWN_AT_MS],
        )
    }.first()

    suspend fun onGameCompleted() {
        dataStore.edit { it[GAMES_SINCE_LAST_AD] = (it[GAMES_SINCE_LAST_AD] ?: 0) + 1 }
    }

    suspend fun onInterstitialShown(nowMs: Long) {
        dataStore.edit {
            it[GAMES_SINCE_LAST_AD] = 0
            it[LAST_SHOWN_AT_MS] = nowMs
        }
    }

    private companion object {
        val GAMES_SINCE_LAST_AD = intPreferencesKey("interstitial_games_since_last_ad")
        val LAST_SHOWN_AT_MS = longPreferencesKey("interstitial_last_shown_at_ms")
    }
}
