package com.dev.goodluckcy.brainup.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.feature.game.GamePlaceholderScreen
import com.dev.goodluckcy.brainup.feature.home.HomeScreen
import com.dev.goodluckcy.brainup.feature.numbermemory.NumberMemoryScreen
import com.dev.goodluckcy.brainup.feature.reaction.ReactionScreen
import com.dev.goodluckcy.brainup.feature.result.ResultScreen
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
            val gameType = entry.toRoute<GameRoute>().gameType
            val onBack: () -> Unit = { navController.popBackStack() }
            val onFinish: (GameResult) -> Unit = { result ->
                navController.navigate(ResultRoute.from(result)) {
                    popUpTo<GameRoute> { inclusive = true }
                }
            }
            when (gameType) {
                GameType.NUMBER_MEMORY -> NumberMemoryScreen(onBack = onBack, onFinish = onFinish)
                GameType.REACTION -> ReactionScreen(onBack = onBack, onFinish = onFinish)
                GameType.PATTERN -> GamePlaceholderScreen(gameType = gameType, onBack = onBack)
            }
        }
        composable<ResultRoute> { entry ->
            val result = entry.toRoute<ResultRoute>().toGameResult()
            val playGame: (GameType) -> Unit = { gameType ->
                navController.navigate(GameRoute(gameType)) {
                    popUpTo<ResultRoute> { inclusive = true }
                }
            }
            ResultScreen(
                result = result,
                onRetry = { playGame(result.gameType) },
                onNextGame = playGame,
                onHome = { navController.popBackStack<HomeRoute>(inclusive = false) },
            )
        }
    }
}
