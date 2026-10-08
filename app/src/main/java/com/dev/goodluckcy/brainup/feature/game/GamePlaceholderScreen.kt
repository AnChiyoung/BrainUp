package com.dev.goodluckcy.brainup.feature.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.PlaceholderScreen
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.GameType

// TODO(Day 2~5): 게임별 Screen/ViewModel/Engine으로 교체
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlaceholderScreen(
    gameType: GameType,
    onBack: () -> Unit,
) {
    val title = stringResource(gameType.titleRes)
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            },
        )
        PlaceholderScreen(
            title = title,
            message = stringResource(R.string.placeholder_coming_soon),
        )
    }
}
