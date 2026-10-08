package com.dev.goodluckcy.brainup.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dev.goodluckcy.brainup.feature.game.GamePlaceholderScreen
import com.dev.goodluckcy.brainup.feature.home.HomeScreen
import com.dev.goodluckcy.brainup.feature.settings.SettingsScreen
import com.dev.goodluckcy.brainup.feature.stats.StatsScreen

@Composable
fun BrainUpNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onGameClick = { gameType -> navController.navigate(GameRoute(gameType)) },
                onSettingsClick = { navController.navigateToTopLevel(TopLevelDestination.SETTINGS) },
            )
        }
        composable<StatsRoute> { StatsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
        composable<GameRoute> { entry ->
            GamePlaceholderScreen(
                gameType = entry.toRoute<GameRoute>().gameType,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
