package com.dev.goodluckcy.brainup.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.goodluckcy.brainup.R
import kotlin.reflect.KClass

/** 하단 내비게이션에 노출되는 최상위 화면. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    HOME(HomeRoute, HomeRoute::class, Icons.Filled.Home, R.string.nav_home),
    STATS(StatsRoute, StatsRoute::class, Icons.AutoMirrored.Filled.List, R.string.nav_stats),
    SETTINGS(SettingsRoute, SettingsRoute::class, Icons.Filled.Settings, R.string.nav_settings),
}
