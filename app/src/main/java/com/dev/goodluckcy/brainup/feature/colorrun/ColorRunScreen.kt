package com.dev.goodluckcy.brainup.feature.colorrun

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.brainyBackground
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconSpec
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.component.OutlineWidth
import com.dev.goodluckcy.brainup.core.designsystem.component.RibbonBanner
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Danger
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.MintDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightPath
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.PinkDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import com.dev.goodluckcy.brainup.feature.game.GameScaffold
import com.dev.goodluckcy.brainup.feature.game.PrimaryGameButton
import com.dev.goodluckcy.brainup.feature.shop.EquippedItemsViewModel

@Composable
fun ColorRunScreen(
    onBack: () -> Unit,
    onFinish: (GameResult) -> Unit,
    viewModel: ColorRunViewModel = hiltViewModel(),
    equippedItems: EquippedItemsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inventory by equippedItems.inventory.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }

    ColorRunContent(
        uiState = uiState,
        brainy = inventory.equippedIn(ItemSlot.BRAINY_COLOR),
        onBack = onBack,
        onStart = viewModel::start,
        onResume = viewModel::resume,
        onColorTap = viewModel::onColorTap,
        onShowResult = { onFinish(viewModel.result()) },
    )
}

@Composable
private fun ColorRunContent(
    uiState: ColorRunUiState,
    brainy: ShopItem,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onColorTap: (Int) -> Unit,
    onShowResult: () -> Unit,
) {
    val run = uiState.run
    // 결승선 통과 세리머니: 배경은 멈추고 브레니만 깡충깡충 앞으로 나아간다.
    val finish = remember { Animatable(0f) }
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == ColorRunPhase.Finished) {
            finish.animateTo(1f, tween(durationMillis = FINISH_CEREMONY_MS, easing = LinearEasing))
        }
    }
    GameScaffold(
        title = stringResource(R.string.game_color_run),
        onBack = onBack,
        skyVariant = 0,
        trailing = {
            HudChip(text = formatRunTime(run?.elapsedMs ?: 0L)) {
                GameIcon(icon = GameIcons.Flag, size = 18.dp, tint = Pink, strokeWidth = 2.6f)
            }
        },
    ) {
        RibbonBanner(
            text = if (run == null) {
                stringResource(R.string.ribbon_ready)
            } else {
                stringResource(
                    R.string.color_run_gate_progress,
                    run.openedGates,
                    ColorRunEngine.GATE_COUNT,
                )
            },
            color = Pink,
            tailColor = PinkDark,
        )
        Text(
            text = instructionText(uiState),
            style = MaterialTheme.typography.titleLarge,
            color = if ((run?.stunRemainingMs ?: 0L) > 0) Danger else Color.White,
            textAlign = TextAlign.Center,
        )
        RunScene(
            run = run,
            brainy = brainy,
            finishProgress = finish.value,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        when (uiState.phase) {
            ColorRunPhase.Ready -> PrimaryGameButton(stringResource(R.string.action_start), onStart)
            ColorRunPhase.Paused -> PrimaryGameButton(stringResource(R.string.color_run_resume), onResume)
            ColorRunPhase.Finished -> PrimaryGameButton(stringResource(R.string.action_show_result), onShowResult)
            ColorRunPhase.Running -> ColorButtons(run = run, onColorTap = onColorTap)
        }
    }
}

@Composable
private fun instructionText(uiState: ColorRunUiState): String {
    val run = uiState.run
    return when {
        uiState.phase == ColorRunPhase.Ready -> stringResource(R.string.color_run_rules)
        uiState.phase == ColorRunPhase.Paused -> stringResource(R.string.color_run_paused)
        uiState.phase == ColorRunPhase.Finished -> stringResource(R.string.color_run_finished)
        run == null -> ""
        run.stunRemainingMs > 0 -> stringResource(R.string.color_run_stunned)
        run.blocked -> stringResource(R.string.color_run_blocked)
        run.nextGateColor == null -> stringResource(R.string.color_run_last_stretch)
        run.combo >= 2 -> stringResource(R.string.color_run_combo, run.combo)
        else -> stringResource(R.string.color_run_go)
    }
}

@Composable
private fun ColorButtons(run: ColorRunState?, onColorTap: (Int) -> Unit) {
    val order = run?.buttonOrder ?: return
    val stunned = run.stunRemainingMs > 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        order.forEach { colorIndex ->
            val color = RunColors[colorIndex]
            val description = stringResource(R.string.color_run_button_desc, stringResource(color.nameRes))
            GameButton(
                onClick = { onColorTap(colorIndex) },
                modifier = Modifier
                    .weight(1f)
                    .height(BUTTON_HEIGHT),
                color = color.face,
                enabled = !stunned,
                onClickLabel = description,
                contentPadding = PaddingValues(0.dp),
            ) {
                GameIcon(icon = color.icon, size = 30.dp, tint = Ink, fill = Color.White, strokeWidth = 2f)
            }
        }
    }
}

private fun formatRunTime(ms: Long): String {
    val tenths = ms / 100
    return "%d.%d".format(tenths / 10, tenths % 10)
}

private val BUTTON_HEIGHT = 76.dp
private const val FINISH_CEREMONY_MS = 2_000

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ColorRunRunningPreview() {
    BrainUpTheme {
        ColorRunContent(
            uiState = ColorRunUiState(
                phase = ColorRunPhase.Running,
                run = ColorRunState(
                    gateColors = listOf(0, 1, 2, 3, 0, 2, 1, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3),
                    buttonOrder = listOf(2, 0, 3, 1),
                    distance = 26f,
                    openedGates = 2,
                    combo = 2,
                    elapsedMs = 6_400,
                ),
            ),
            brainy = ShopItem.BRAINY_PINK,
            onBack = {},
            onStart = {},
            onResume = {},
            onColorTap = {},
            onShowResult = {},
        )
    }
}
