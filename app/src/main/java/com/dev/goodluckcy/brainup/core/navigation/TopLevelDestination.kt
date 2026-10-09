package com.dev.goodluckcy.brainup.core.navigation

import androidx.annotation.StringRes
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconSpec
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import kotlin.reflect.KClass

/** 하단 탭에 노출되는 최상위 화면. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: GameIconSpec,
    @param:StringRes val labelRes: Int,
) {
    HOME(HomeRoute, HomeRoute::class, GameIcons.Map, R.string.nav_home),
    STATS(StatsRoute, StatsRoute::class, GameIcons.Trophy, R.string.nav_stats),
    SHOP(ShopRoute, ShopRoute::class, GameIcons.Store, R.string.nav_shop),
    SETTINGS(SettingsRoute, SettingsRoute::class, GameIcons.Sliders, R.string.nav_settings),
}
