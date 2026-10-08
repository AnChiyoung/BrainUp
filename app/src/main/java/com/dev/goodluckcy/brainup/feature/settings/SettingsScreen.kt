package com.dev.goodluckcy.brainup.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.PlaceholderScreen

// TODO: 소리/진동(DataStore), 광고 동의(UMP), 개인정보처리방침, 앱 버전
@Composable
fun SettingsScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.nav_settings),
        message = stringResource(R.string.placeholder_coming_soon),
    )
}
