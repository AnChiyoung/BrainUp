package com.dev.goodluckcy.brainup.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.goodluckcy.brainup.core.navigation.BrainUpNavHost
import com.dev.goodluckcy.brainup.core.navigation.TopLevelDestination
import com.dev.goodluckcy.brainup.core.navigation.isTopLevel
import com.dev.goodluckcy.brainup.core.navigation.navigateToTopLevel

@Composable
fun BrainUpApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // 게임 플레이 화면 등 최상위가 아닌 화면에서는 하단 탭을 숨긴다.
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isTopLevel(it) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 상단 인셋은 각 화면의 TopAppBar가 처리한다.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        NavigationBarItem(
                            selected = currentDestination.isTopLevel(destination),
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        BrainUpNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
