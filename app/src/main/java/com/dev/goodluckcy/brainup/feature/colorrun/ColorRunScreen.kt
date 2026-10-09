package com.dev.goodluckcy.brainup.feature.colorrun

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
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

/** 문·버튼 색. 색만으로 구분하기 어려운 사람을 위해 모양 아이콘도 함께 쓴다. */
private class RunColor(val face: Color, val dark: Color, val icon: GameIconSpec, @StringRes val nameRes: Int)

private val RunColors = listOf(
    RunColor(Pink, PinkDark, GameIcons.Heart, R.string.color_run_color_pink),
    RunColor(Sun, SunDark, GameIcons.Star, R.string.color_run_color_yellow),
    RunColor(Mint, MintDark, GameIcons.Sparkle, R.string.color_run_color_mint),
    RunColor(Sky, SkyDark, GameIcons.Bolt, R.string.color_run_color_sky),
)

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
        RunTrack(
            run = run,
            brainy = brainy,
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

/** 옆에서 본 달리기 길: 브레니는 왼쪽에 있고 문과 결승선이 오른쪽에서 다가온다. */
@Composable
private fun RunTrack(run: ColorRunState?, brainy: ShopItem, modifier: Modifier = Modifier) {
    val distance = run?.distance ?: 0f
    val stunned = (run?.stunRemainingMs ?: 0L) > 0
    // 넘어지면 옆으로 기운다.
    val tilt by animateFloatAsState(if (stunned) -24f else 0f, tween(120), label = "tilt")
    BoxWithConstraints(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(NightDeep),
    ) {
        val groundY = maxHeight * 0.72f
        val runnerSize = minOf(104.dp, maxHeight * 0.2f)
        val iconPaths = remember {
            RunColors.map { color -> color.icon.paths.map { PathParser().parsePathString(it).toPath() } }
        }
        Canvas(Modifier.fillMaxSize()) {
            drawTrack(run = run, distance = distance, groundY = groundY.toPx(), iconPaths = iconPaths)
        }
        // 달리는 동안 통통 튄다(거리에 따라 위아래).
        val bob = if (run != null && !stunned && !run.blocked && !run.isFinished) {
            -kotlin.math.abs(kotlin.math.sin(distance * 2.2f)) * 10f
        } else {
            0f
        }
        Box(
            modifier = Modifier
                .offset(x = maxWidth * RUNNER_X - runnerSize / 2, y = groundY - runnerSize)
                .graphicsLayer {
                    translationY = bob.dp.toPx()
                    rotationZ = tilt
                }
                .size(runnerSize)
                .chunky(color = Color.Transparent, shape = RoundedCornerShape(18.dp), depth = 0.dp)
                .brainyBackground(brainy, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.size(runnerSize * 0.72f))
        }
        ProgressBar(
            progress = run?.progress ?: 0f,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
        )
    }
}

private fun DrawScope.drawTrack(
    run: ColorRunState?,
    distance: Float,
    groundY: Float,
    iconPaths: List<List<Path>>,
) {
    val metersToPx = size.width / VIEW_METERS
    fun xOf(meters: Float) = size.width * RUNNER_X + (meters - distance) * metersToPx
    val stroke = OutlineWidth.toPx()

    // 땅과 길 무늬
    drawRect(NightDeeper, Offset(0f, groundY), Size(size.width, size.height - groundY))
    drawLine(Ink, Offset(0f, groundY), Offset(size.width, groundY), strokeWidth = stroke)
    val stripe = 4f
    var m = (distance / stripe).toInt() * stripe - stripe * 2
    while (xOf(m) < size.width) {
        drawRoundRect(
            NightPath,
            Offset(xOf(m), groundY + 18.dp.toPx()),
            Size(stripe * metersToPx * 0.5f, 6.dp.toPx()),
            CornerRadius(3.dp.toPx()),
        )
        m += stripe
    }

    // 결승선(체크무늬 기둥)
    val finishX = xOf(ColorRunEngine.FINISH_DISTANCE)
    if (finishX < size.width + 40f) {
        val cell = (groundY * 0.06f).coerceAtLeast(10.dp.toPx())
        val top = groundY - cell * 10
        for (row in 0 until 10) {
            for (col in 0 until 2) {
                val color = if ((row + col) % 2 == 0) Color.White else Ink
                drawRect(color, Offset(finishX + col * cell, top + row * cell), Size(cell, cell))
            }
        }
        drawRect(Ink, Offset(finishX, top), Size(cell * 2, cell * 10), style = Stroke(stroke))
    }

    // 색 문: 열린 문은 땅으로 내려가 흐려진다.
    if (run != null) {
        // 길 높이에 맞춰 문 크기를 정한다(브레니보다 확실히 크게).
        val gateH = groundY * 0.5f
        val gateW = (gateH * 0.28f).coerceIn(36.dp.toPx(), 56.dp.toPx())
        run.gateColors.forEachIndexed { index, colorIndex ->
            val x = xOf(ColorRunEngine.gatePosition(index))
            if (x < -gateW || x > size.width + gateW) return@forEachIndexed
            val color = RunColors[colorIndex]
            val opened = index < run.openedGates
            val h = if (opened) gateH * 0.18f else gateH
            val alpha = if (opened) 0.45f else 1f
            val topLeft = Offset(x, groundY - h)
            drawRoundRect(color.dark.copy(alpha = alpha), topLeft + Offset(0f, 6.dp.toPx()), Size(gateW, h), CornerRadius(10.dp.toPx()))
            drawRoundRect(color.face.copy(alpha = alpha), topLeft, Size(gateW, h), CornerRadius(10.dp.toPx()))
            drawRoundRect(Ink.copy(alpha = alpha), topLeft, Size(gateW, h), CornerRadius(10.dp.toPx()), style = Stroke(stroke))
            if (!opened) {
                // 버튼과 같은 모양 아이콘(색 구분 보조)
                val iconSize = gateW * 0.72f
                translate(left = x + (gateW - iconSize) / 2, top = groundY - h * 0.6f - iconSize / 2) {
                    scale(iconSize / 24f, pivot = Offset.Zero) {
                        iconPaths[colorIndex].forEach { path ->
                            drawPath(path, Color.White)
                            drawPath(path, Ink, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(12.dp)
            .background(NightDeeper, RoundedCornerShape(999.dp)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(12.dp)
                .background(Pink, RoundedCornerShape(999.dp)),
        )
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

/** 화면 너비 대비 브레니 위치 */
private const val RUNNER_X = 0.2f

/** 화면 너비에 보이는 거리(m) */
private const val VIEW_METERS = 26f

private val BUTTON_HEIGHT = 76.dp

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
