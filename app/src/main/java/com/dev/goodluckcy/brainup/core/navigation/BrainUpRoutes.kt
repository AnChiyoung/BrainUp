package com.dev.goodluckcy.brainup.core.navigation

import com.dev.goodluckcy.brainup.domain.model.GameType
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute

@Serializable
data class GameRoute(val gameType: GameType)
