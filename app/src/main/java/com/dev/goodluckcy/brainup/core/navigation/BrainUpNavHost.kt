package com.dev.goodluckcy.brainup.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.feature.home.HomeScreen
import com.dev.goodluckcy.brainup.feature.numbermemory.NumberMemoryScreen
import com.dev.goodluckcy.brainup.feature.pattern.PatternMemoryScreen
import com.dev.goodluckcy.brainup.feature.reaction.ReactionScreen
import com.dev.goodluckcy.brainup.feature.result.ResultScreen
import com.dev.goodluckcy.brainup.feature.shop.ShopScreen
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
        // 기본 전환(0.7초 크로스페이드) 대신 짧은 페이드로 화면을 바꾼다.
        enterTransition = { fadeIn(tween(SCREEN_FADE_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_FADE_MS)) },
        popEnterTransition = { fadeIn(tween(SCREEN_FADE_MS)) },
        popExitTransition = { fadeOut(tween(SCREEN_FADE_MS)) },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onGameClick = { gameType -> navController.navigate(GameRoute(gameType)) },
                onOpenShop = { navController.navigateToTopLevel(TopLevelDestination.SHOP) },
            )
        }
        composable<StatsRoute> { StatsScreen() }
        composable<ShopRoute> { ShopScreen() }
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
                GameType.PATTERN -> PatternMemoryScreen(onBack = onBack, onFinish = onFinish)
            }
        }
        composable<ResultRoute> {
            val playGame: (GameType) -> Unit = { gameType ->
                navController.navigate(GameRoute(gameType)) {
                    popUpTo<ResultRoute> { inclusive = true }
                }
            }
            ResultScreen(
                onRetry = playGame,
                onNextGame = playGame,
                onHome = { navController.popBackStack<HomeRoute>(inclusive = false) },
            )
        }
    }
}

private const val SCREEN_FADE_MS = 200
