package com.dev.goodluckcy.brainup.feature.reaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Danger
import com.dev.goodluckcy.brainup.core.designsystem.theme.Go
import com.dev.goodluckcy.brainup.core.designsystem.theme.GoDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.feature.game.GameScaffold
import com.dev.goodluckcy.brainup.feature.game.PrimaryGameButton

@Composable
fun ReactionScreen(
    onBack: () -> Unit,
    onFinish: (GameResult) -> Unit,
    viewModel: ReactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }

    ReactionContent(
        uiState = uiState,
        onBack = onBack,
        onTap = viewModel::onTap,
        onShowResult = { onFinish(viewModel.result()) },
    )
}

@Composable
private fun ReactionContent(
    uiState: ReactionUiState,
    onBack: () -> Unit,
    onTap: () -> Unit,
    onShowResult: () -> Unit,
) {
    GameScaffold(
        title = stringResource(R.string.game_reaction),
        onBack = onBack,
        skyVariant = 0,
        trailing = {
            uiState.reactionsMs.minOrNull()?.let { best ->
                HudChip(text = stringResource(R.string.unit_ms, best)) {
                    GameIcon(icon = GameIcons.Bolt, size = 18.dp, tint = Ink, fill = Sun, strokeWidth = 1.6f)
                }
            }
        },
    ) {
        AttemptLamps(completed = uiState.completedAttempts, total = uiState.totalAttempts)
        if (uiState.phase == ReactionPhase.Finished) {
            FinishedContent(uiState, Modifier.weight(1f))
            PrimaryGameButton(stringResource(R.string.action_show_result), onShowResult)
        } else {
            ArcadeTapButton(
                uiState = uiState,
                onTap = onTap,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            AttemptChips(uiState.reactionsMs)
        }
    }
}

/** 시도 5회를 신호등처럼 보여준다. 지금 시도는 크게 빛난다. */
@Composable
private fun AttemptLamps(completed: Int, total: Int) {
    Row(
        modifier = Modifier
            .background(NightDeep, RoundedCornerShape(999.dp))
            .border(3.dp, Ink, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val isCurrent = i == completed
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 28.dp else 22.dp)
                    .drawBehind {
                        if (isCurrent) drawCircle(Sun.copy(alpha = 0.35f), size.minDimension / 2 + 5.dp.toPx())
                    }
                    .background(
                        when {
                            i < completed -> Mint
                            isCurrent -> Sun
                            else -> NightDeeper
                        },
                        CircleShape,
                    )
                    .border(3.dp, Ink, CircleShape),
            )
        }
    }
}

/** 화면 대부분을 차지하는 커다란 아케이드 버튼. 손가락이 닿는 순간(down)에 측정한다. */
@Composable
private fun ArcadeTapButton(
    uiState: ReactionUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (face, rim) = when (uiState.phase) {
        ReactionPhase.Waiting -> Danger to Color(0xFFB8262D)
        ReactionPhase.Go -> Go to GoDark
        ReactionPhase.TooEarly -> Pink to Color(0xFFD63A68)
        ReactionPhase.AttemptResult -> Sky to Color(0xFF339AF0)
        else -> Sun to Color(0xFFE0A800)
    }
    val textColor = if (uiState.phase == ReactionPhase.Waiting) Color.White else Ink
    val currentOnTap by rememberUpdatedState(onTap)
    val shape = RoundedCornerShape(40.dp)
    Box(
        modifier = modifier
            .padding(bottom = 12.dp)
            .chunky(color = face, shape = shape, depth = 12.dp, borderWidth = 4.dp)
            .drawBehind {
                // 아래쪽 테두리 음영 + 왼쪽 위 하이라이트로 입체감
                val inset = 4.dp.toPx()
                drawRoundRect(
                    color = rim,
                    topLeft = Offset(inset, size.height - 18.dp.toPx()),
                    size = Size(size.width - inset * 2, 14.dp.toPx()),
                    cornerRadius = CornerRadius(36.dp.toPx()),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(26.dp.toPx(), 22.dp.toPx()),
                    size = Size(70.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = CornerRadius(9.dp.toPx()),
                )
            }
            .pointerInput(Unit) { detectTapGestures(onPress = { currentOnTap() }) },
        contentAlignment = Alignment.Center,
    ) {
        val (title, subtitle) = tapAreaText(uiState)
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (uiState.phase == ReactionPhase.Go || uiState.phase == ReactionPhase.Ready) {
                GameIcon(icon = GameIcons.Touch, size = 80.dp, tint = textColor, strokeWidth = 2f)
            }
            Text(
                text = title,
                style = if (uiState.phase == ReactionPhase.Go) {
                    MaterialTheme.typography.displayLarge
                } else {
                    MaterialTheme.typography.displaySmall
                },
                color = textColor,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun tapAreaText(uiState: ReactionUiState): Pair<String, String?> = when (uiState.phase) {
    ReactionPhase.Ready -> if (uiState.completedAttempts == 0) {
        stringResource(R.string.reaction_tap_to_start) to stringResource(R.string.reaction_rules)
    } else {
        stringResource(R.string.reaction_tap_to_resume) to null
    }
    ReactionPhase.Waiting -> stringResource(R.string.reaction_wait) to stringResource(R.string.reaction_wait_hint)
    ReactionPhase.Go -> stringResource(R.string.reaction_go) to null
    ReactionPhase.TooEarly -> stringResource(R.string.reaction_too_early) to stringResource(R.string.reaction_too_early_hint)
    ReactionPhase.AttemptResult -> stringResource(R.string.unit_ms, uiState.lastReactionMs ?: 0L) to
        stringResource(R.string.reaction_tap_to_continue)
    ReactionPhase.Finished -> "" to null
}

/** 이번 판 기록 칩. 가장 빠른 기록은 금테. */
@Composable
private fun AttemptChips(reactionsMs: List<Long>) {
    val best = reactionsMs.minOrNull()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        reactionsMs.forEachIndexed { i, ms ->
            val isBest = ms == best
            Column(
                modifier = Modifier
                    .background(NightDeep, RoundedCornerShape(14.dp))
                    .border(2.dp, if (isBest) Sun else NightLight, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.reaction_attempt_n, i + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isBest) Sun else Lavender,
                )
                Text(text = ms.toString(), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun FinishedContent(uiState: ReactionUiState, modifier: Modifier = Modifier) {
    GamePanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(stringResource(R.string.reaction_median), style = MaterialTheme.typography.titleLarge, color = Lavender)
            Text(
                text = stringResource(R.string.unit_ms, uiState.medianMs ?: 0L),
                style = MaterialTheme.typography.displayMedium,
                color = Sun,
            )
            AttemptChips(uiState.reactionsMs)
            if (uiState.falseStarts > 0) {
                Text(
                    text = stringResource(R.string.reaction_false_starts, uiState.falseStarts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Lavender,
                )
            }
            Text(
                text = stringResource(R.string.reaction_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = Lavender,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ReactionGoPreview() {
    BrainUpTheme {
        ReactionContent(
            uiState = ReactionUiState(phase = ReactionPhase.Go, reactionsMs = listOf(312, 287)),
            onBack = {},
            onTap = {},
            onShowResult = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ReactionFinishedPreview() {
    BrainUpTheme {
        ReactionContent(
            uiState = ReactionUiState(
                phase = ReactionPhase.Finished,
                reactionsMs = listOf(312, 287, 301, 265, 344),
                falseStarts = 1,
                medianMs = 301,
                bestMs = 265,
                score = 699,
            ),
            onBack = {},
            onTap = {},
            onShowResult = {},
        )
    }
}
