package com.dev.goodluckcy.brainup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.navigation.BrainUpNavHost
import com.dev.goodluckcy.brainup.core.navigation.TopLevelDestination
import com.dev.goodluckcy.brainup.core.navigation.isTopLevel
import com.dev.goodluckcy.brainup.core.navigation.navigateToTopLevel

@Composable
fun BrainUpApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // 게임 플레이·결과 화면에서는 하단 탭을 숨긴다.
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isTopLevel(it) }

    // 탭 바는 화면 위에 얹는다. 탭 바가 나타나고 사라져도 화면 크기가 바뀌지 않아
    // 전환 중에 이전 화면이 늘어나 보이지 않는다. 탭 화면은 tabBarPadding()으로 여백을 둔다.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Night),
    ) {
        BrainUpNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = showBottomBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(TAB_BAR_ANIM_MS)) { it } + fadeIn(tween(TAB_BAR_ANIM_MS)),
            exit = slideOutVertically(tween(TAB_BAR_ANIM_MS)) { it } + fadeOut(tween(TAB_BAR_ANIM_MS)),
        ) {
            GameTabBar(
                isSelected = { currentDestination.isTopLevel(it) },
                onSelect = { navController.navigateToTopLevel(it) },
            )
        }
    }
}

private const val TAB_BAR_ANIM_MS = 200

/** 떠 있는 어두운 탭 바. 선택된 탭은 노란 알약으로 표시한다. */
@Composable
private fun GameTabBar(
    isSelected: (TopLevelDestination) -> Boolean,
    onSelect: (TopLevelDestination) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp)
            .background(Ink, RoundedCornerShape(22.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = isSelected(destination)
            val contentColor = if (selected) Ink else Color.White
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(if (selected) Sun else Color.Transparent, RoundedCornerShape(16.dp))
                    .semantics { this.selected = selected }
                    .clickable(role = Role.Tab) { onSelect(destination) },
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GameIcon(icon = destination.icon, size = 22.dp, tint = contentColor)
                Text(
                    text = stringResource(destination.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                )
            }
        }
    }
}
