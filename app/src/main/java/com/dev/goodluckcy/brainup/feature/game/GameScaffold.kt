package com.dev.goodluckcy.brainup.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky

/**
 * 게임 화면 공통 틀: 밤하늘 배경, 시스템 바 여백, 상단 바(뒤로가기 + 제목 + 오른쪽 칩).
 * 시스템 뒤로가기는 막으며, 화면을 나가는 방법은 상단 뒤로가기 버튼([onBack])뿐이다.
 */
@Composable
fun GameScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    skyVariant: Int = 1,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    // 게임 중 실수로 나가지 않도록 시스템 뒤로가기(제스처·키)는 막고 상단 버튼으로만 나간다.
    BackHandler(enabled = true) {}
    Column(
        modifier = modifier
            .fillMaxSize()
            .nightSky(variant = skyVariant)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIconButton(
                icon = GameIcons.Back,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
            )
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            trailing()
        }
        content()
    }
}
