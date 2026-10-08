package com.dev.goodluckcy.brainup.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.core.designsystem.component.CoinDot
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun

/** 화면 하단의 큰 노란 행동 버튼 */
@Composable
fun PrimaryGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Sun,
) {
    GameButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp),
        color = color,
        depth = 7.dp,
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/** 이번 판 점수 칩 */
@Composable
fun ScoreChip(score: Int) {
    HudChip(text = score.toString()) { CoinDot() }
}

/** 이어하기(하트) 남은 횟수 칩 */
@Composable
fun HeartChip(remaining: Int) {
    HudChip(text = remaining.toString()) {
        GameIcon(icon = GameIcons.Heart, size = 18.dp, tint = Ink, fill = Pink, strokeWidth = 1.6f)
    }
}

/** 진행 점: 완료 [done]개, 전체 [total]개 */
@Composable
fun ProgressDots(done: Int, total: Int, color: Color = Sun, size: Dp = 14.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(size)
                    .background(if (i < done) color else NightDeep, CircleShape)
                    .border(3.dp, if (i < done) Ink else NightLight, CircleShape),
            )
        }
    }
}
