package com.dev.goodluckcy.brainup.feature.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.PlaceholderScreen

// TODO(Day 6~7): 연속 완료 일수, 게임별 최고 기록, 최근 7일 통계
@Composable
fun StatsScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.nav_stats),
        message = stringResource(R.string.placeholder_coming_soon),
    )
}
