package com.dev.goodluckcy.brainup.feature.colorrun

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.brainyBrush
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconSpec
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.MintDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightPath
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.PinkDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** 문·버튼 색. 색만으로 구분하기 어려운 사람을 위해 모양 아이콘도 함께 쓴다. */
internal class RunColor(val face: Color, val dark: Color, val icon: GameIconSpec, @param:StringRes val nameRes: Int)

internal val RunColors = listOf(
    RunColor(Pink, PinkDark, GameIcons.Heart, R.string.color_run_color_pink),
    RunColor(Sun, SunDark, GameIcons.Star, R.string.color_run_color_yellow),
    RunColor(Mint, MintDark, GameIcons.Sparkle, R.string.color_run_color_mint),
    RunColor(Sky, SkyDark, GameIcons.Bolt, R.string.color_run_color_sky),
)

/**
 * 브레니 뒤에서 앞을 보고 달리는 장면.
 * 길이 소실점으로 모이고, 색 문과 결승선이 멀리서 다가온다.
 */
@Composable
internal fun RunScene(
    run: ColorRunState?,
    brainy: ShopItem,
    modifier: Modifier = Modifier,
    /** 결승선 통과 후 세리머니 진행(0~1) */
    finishProgress: Float = 0f,
) {
    val iconPaths = remember {
        RunColors.map { color -> color.icon.paths.map { PathParser().parsePathString(it).toPath() } }
    }
    val brush = brainy.brainyBrush
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val scene = SceneProjection(size.width, size.height, run?.distance ?: 0f)
            drawSky(scene)
            drawRoad(scene)
            drawRoadside(scene)
            val celebrating = run?.isFinished == true
            // 결승선을 지나면 브레니가 아치 너머로 멀어지므로 아치보다 먼저 그린다.
            if (celebrating) drawRunner(scene, run, brush, finishProgress)
            drawFinish(scene)
            if (run != null) drawGates(scene, run, iconPaths)
            if (run != null && run.combo >= SPEED_LINE_COMBO && !run.blocked && !celebrating) drawSpeedLines(scene, run)
            if (!celebrating) drawRunner(scene, run, brush, 0f)
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

/** 원근 투영: [distance]에 서 있는 브레니 바로 뒤 카메라 기준 */
private class SceneProjection(val width: Float, val height: Float, val distance: Float) {
    val horizonY = height * HORIZON
    val footY = height * FOOT
    val centerX = width / 2

    /** 카메라에서 [meters] 지점까지의 깊이 */
    fun depth(meters: Float) = meters - distance + CAMERA_BACK

    /** 브레니 자리에서 1, 멀어질수록 작아지는 배율 */
    fun scaleAt(depth: Float) = CAMERA_BACK / depth

    fun groundY(depth: Float) = horizonY + (footY - horizonY) * scaleAt(depth)

    fun roadHalf(depth: Float) = width * ROAD_HALF * scaleAt(depth)
}

private fun DrawScope.drawSky(scene: SceneProjection) {
    drawRect(Brush.verticalGradient(listOf(NightDeeper, Night), endY = scene.horizonY), size = Size(size.width, scene.horizonY))
    // 멀리 보이는 행성과 별
    drawCircle(NightLight, size.width * 0.16f, Offset(size.width * 0.82f, scene.horizonY * 0.62f))
    drawCircle(Sun.copy(alpha = 0.9f), size.width * 0.035f, Offset(size.width * 0.2f, scene.horizonY * 0.45f))
    listOf(0.08f to 0.3f, 0.35f to 0.18f, 0.55f to 0.42f, 0.68f to 0.15f, 0.92f to 0.28f).forEach { (x, y) ->
        drawCircle(Color.White, 2.dp.toPx(), Offset(size.width * x, scene.horizonY * y))
    }
    // 땅
    drawRect(NightDeep, Offset(0f, scene.horizonY), Size(size.width, size.height - scene.horizonY))
}

private fun DrawScope.drawRoad(scene: SceneProjection) {
    val near = 0.6f
    val far = VIEW_AHEAD
    val road = Path().apply {
        moveTo(scene.centerX - scene.roadHalf(near), scene.groundY(near))
        lineTo(scene.centerX - scene.roadHalf(far), scene.groundY(far))
        lineTo(scene.centerX + scene.roadHalf(far), scene.groundY(far))
        lineTo(scene.centerX + scene.roadHalf(near), scene.groundY(near))
        close()
    }
    drawPath(road, NightPath)

    // 길 가장자리 줄무늬(빨강·흰색)와 가운데 점선이 다가와 속도감을 준다.
    val start = ((scene.distance - CAMERA_BACK) / STRIPE).toInt() * STRIPE
    var m = start
    var index = (start / STRIPE).toInt()
    while (scene.depth(m) < VIEW_AHEAD) {
        val z1 = scene.depth(m)
        val z2 = scene.depth(m + STRIPE)
        if (z1 > near) {
            val curbColor = if (index % 2 == 0) Pink else Color.White
            listOf(-1f, 1f).forEach { side ->
                drawQuad(
                    Offset(scene.centerX + side * scene.roadHalf(z1), scene.groundY(z1)),
                    Offset(scene.centerX + side * scene.roadHalf(z1) * 0.9f, scene.groundY(z1)),
                    Offset(scene.centerX + side * scene.roadHalf(z2) * 0.9f, scene.groundY(z2)),
                    Offset(scene.centerX + side * scene.roadHalf(z2), scene.groundY(z2)),
                    curbColor,
                )
            }
            if (index % 2 == 0) {
                val dashZ2 = scene.depth(m + STRIPE * 0.5f)
                drawQuad(
                    Offset(scene.centerX - scene.roadHalf(z1) * 0.03f, scene.groundY(z1)),
                    Offset(scene.centerX + scene.roadHalf(z1) * 0.03f, scene.groundY(z1)),
                    Offset(scene.centerX + scene.roadHalf(dashZ2) * 0.03f, scene.groundY(dashZ2)),
                    Offset(scene.centerX - scene.roadHalf(dashZ2) * 0.03f, scene.groundY(dashZ2)),
                    Color.White.copy(alpha = 0.5f),
                )
            }
        }
        m += STRIPE
        index++
    }
}

/** 길가 나무가 양옆으로 휙휙 지나간다. */
private fun DrawScope.drawRoadside(scene: SceneProjection) {
    val start = ((scene.distance - CAMERA_BACK) / TREE_SPACING).toInt() * TREE_SPACING
    val trees = mutableListOf<Float>()
    var m = start
    while (scene.depth(m) < VIEW_AHEAD) {
        trees += m
        m += TREE_SPACING
    }
    trees.asReversed().forEach { meters ->
        val z = scene.depth(meters)
        if (z < 0.8f) return@forEach
        val s = scene.scaleAt(z)
        val y = scene.groundY(z)
        val index = (meters / TREE_SPACING).toInt()
        listOf(-1f, 1f).forEach { side ->
            val x = scene.centerX + side * (scene.roadHalf(z) + size.width * 0.16f * s)
            val r = size.width * 0.07f * s
            drawLine(Ink, Offset(x, y), Offset(x, y - r * 2.2f), strokeWidth = r * 0.35f, cap = StrokeCap.Round)
            val crown = if ((index + side.toInt()) % 2 == 0) Mint else Sky
            drawCircle(crown, r, Offset(x, y - r * 2.4f))
            drawCircle(Ink, r, Offset(x, y - r * 2.4f), style = Stroke(r * 0.18f))
        }
    }
}

private fun DrawScope.drawFinish(scene: SceneProjection) {
    val z = scene.depth(ColorRunEngine.FINISH_DISTANCE)
    if (z < 0.8f || z > VIEW_AHEAD) return
    val s = scene.scaleAt(z)
    val y = scene.groundY(z)
    val half = scene.roadHalf(z) * 1.1f
    val postH = (scene.footY - scene.horizonY) * GATE_HEIGHT * s
    val post = size.width * 0.03f * s
    listOf(-1f, 1f).forEach { side ->
        drawRect(Color.White, Offset(scene.centerX + side * half - post / 2, y - postH), Size(post, postH))
    }
    // 체크무늬 현수막
    val bannerH = postH * 0.22f
    val cells = 10
    val cellW = half * 2 / cells
    for (row in 0 until 2) {
        for (col in 0 until cells) {
            val color = if ((row + col) % 2 == 0) Color.White else Ink
            drawRect(color, Offset(scene.centerX - half + col * cellW, y - postH + row * bannerH / 2), Size(cellW, bannerH / 2))
        }
    }
    drawRect(Ink, Offset(scene.centerX - half, y - postH), Size(half * 2, bannerH), style = Stroke(3.dp.toPx() * s.coerceAtMost(1f)))
}

private fun DrawScope.drawGates(scene: SceneProjection, run: ColorRunState, iconPaths: List<List<Path>>) {
    val stroke = 3.dp.toPx()
    // 먼 문부터 그린다.
    run.gateColors.indices.reversed().forEach { index ->
        val z = scene.depth(ColorRunEngine.gatePosition(index))
        if (z < CAMERA_BACK * 0.75f || z > VIEW_AHEAD) return@forEach
        val s = scene.scaleAt(z)
        val y = scene.groundY(z)
        val color = RunColors[run.gateColors[index]]
        val opened = index < run.openedGates
        val half = scene.roadHalf(z) * 1.05f
        val h = (scene.footY - scene.horizonY) * GATE_HEIGHT * s
        val post = size.width * 0.05f * s
        val w = stroke * s.coerceAtMost(1.4f)
        // 기둥과 윗보
        listOf(-1f, 1f).forEach { side ->
            val left = scene.centerX + side * half - post / 2
            drawRoundRect(color.face, Offset(left, y - h), Size(post, h), CornerRadius(post / 3))
            drawRoundRect(Ink, Offset(left, y - h), Size(post, h), CornerRadius(post / 3), style = Stroke(w))
        }
        val beamH = post * 1.1f
        drawRoundRect(color.face, Offset(scene.centerX - half - post / 2, y - h - beamH * 0.5f), Size(half * 2 + post, beamH), CornerRadius(beamH / 2))
        drawRoundRect(Ink, Offset(scene.centerX - half - post / 2, y - h - beamH * 0.5f), Size(half * 2 + post, beamH), CornerRadius(beamH / 2), style = Stroke(w))
        if (!opened) {
            // 닫힌 문: 색 판과 큰 아이콘
            val panelTop = y - h + beamH * 0.5f
            val panel = Size(half * 2 - post, h - beamH * 0.5f)
            val panelLeft = scene.centerX - half + post / 2
            drawRect(color.face.copy(alpha = 0.85f), Offset(panelLeft, panelTop), panel)
            drawRect(color.dark, Offset(panelLeft, y - panel.height * 0.12f), Size(panel.width, panel.height * 0.12f))
            val iconSize = minOf(panel.width, panel.height) * 0.42f
            translate(left = scene.centerX - iconSize / 2, top = panelTop + panel.height * 0.42f - iconSize / 2) {
                scale(iconSize / 24f, pivot = Offset.Zero) {
                    iconPaths[run.gateColors[index]].forEach { path ->
                        drawPath(path, Color.White)
                        drawPath(path, Ink, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }
    }
}

/** 콤보가 높으면 화면 가장자리에 바람 줄이 흐른다. */
private fun DrawScope.drawSpeedLines(scene: SceneProjection, run: ColorRunState) {
    val alpha = 0.18f + 0.08f * (run.combo - SPEED_LINE_COMBO)
    repeat(8) { i ->
        val side = if (i % 2 == 0) -1f else 1f
        val t = ((scene.distance * 0.35f + i * 0.37f) % 1f)
        val y = scene.horizonY + (size.height - scene.horizonY) * (0.15f + 0.1f * i)
        val xStart = scene.centerX + side * size.width * (0.25f + 0.25f * t)
        val len = size.width * (0.08f + 0.12f * t)
        drawLine(
            Color.White.copy(alpha = alpha),
            Offset(xStart, y),
            Offset(xStart + side * len, y + len * 0.25f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/**
 * 뒤에서 본 브레니. 거리에 맞춰 다리·팔이 번갈아 움직이고 몸이 통통 튄다.
 * 넘어지면 기울어지고 머리 위로 별이 돈다.
 * 결승선을 통과하면([finishProgress] > 0) 배경은 멈추고 만세를 하며 깡충깡충 앞으로 멀어진다.
 */
private fun DrawScope.drawRunner(scene: SceneProjection, run: ColorRunState?, brush: Brush, finishProgress: Float) {
    val celebrating = run?.isFinished == true
    val depth = CAMERA_BACK + if (celebrating) finishProgress * FINISH_RUN_METERS else 0f
    val s = scene.scaleAt(depth)
    val body = minOf(size.width * 0.24f, (scene.footY - scene.horizonY) * 0.42f) * s
    val stunned = (run?.stunRemainingMs ?: 0L) > 0
    val moving = celebrating || (run != null && !stunned && !run.blocked)
    val stride = if (celebrating) finishProgress * FINISH_STRIDES else (run?.distance ?: 0f) * STRIDE_PER_METER
    val swing = if (moving) sin(stride) else 0f
    val bob = if (moving && !celebrating) abs(cos(stride)) * body * 0.08f else 0f
    val jump = if (celebrating) abs(sin(finishProgress * PI.toFloat() * FINISH_HOPS)) * body * 0.9f else 0f
    val stroke = 3.dp.toPx() * s.coerceAtLeast(0.5f)
    val cx = scene.centerX
    val ground = scene.groundY(depth)
    val foot = ground - jump

    // 그림자(점프하면 작아진다)
    val shadow = 1f - (jump / (body * 1.8f)).coerceIn(0f, 0.5f)
    drawOval(
        Ink.copy(alpha = 0.35f),
        Offset(cx - body * 0.55f * shadow, ground - body * 0.08f),
        Size(body * 1.1f * shadow, body * 0.22f),
    )

    val tilt = if (stunned) -18f else 0f
    rotate(tilt, pivot = Offset(cx, foot)) {
        // 다리: 번갈아 들어 올린다(뒤에서 보면 위아래로 움직인다).
        val legW = body * 0.2f
        val legH = body * 0.34f
        listOf(-1f, 1f).forEach { side ->
            val lift = (if (side < 0) swing else -swing).coerceAtLeast(0f) * body * 0.16f
            val left = cx + side * body * 0.2f - legW / 2
            val top = foot - legH - lift - bob * 0.3f
            drawRoundRect(Ink, Offset(left, top), Size(legW, legH), CornerRadius(legW / 2))
            // 신발 바닥
            drawRoundRect(Color.White, Offset(left - legW * 0.1f, top + legH - legW * 0.45f), Size(legW * 1.2f, legW * 0.45f), CornerRadius(legW / 4))
        }

        val bodyTop = foot - legH * 0.8f - body - bob
        // 팔: 다리와 반대로 흔든다.
        val armW = body * 0.16f
        val armH = body * 0.38f
        listOf(-1f, 1f).forEach { side ->
            // 결승선을 지나면 두 팔을 번쩍 든다.
            val armSwing = if (celebrating) 150f + swing * 15f else (if (side < 0) -swing else swing) * 28f
            val pivot = Offset(cx + side * body * 0.5f, bodyTop + body * 0.42f)
            rotate(armSwing * side, pivot = pivot) {
                drawRoundRect(Ink, Offset(pivot.x - armW / 2, pivot.y), Size(armW, armH), CornerRadius(armW / 2))
            }
        }

        // 몸(뒷모습): 장착한 색 + 외곽선
        val radius = CornerRadius(body * 0.28f)
        drawRoundRect(brush, Offset(cx - body / 2, bodyTop), Size(body, body), radius)
        drawRoundRect(Ink, Offset(cx - body / 2, bodyTop), Size(body, body), radius, style = Stroke(stroke))
        // 머리띠와 바람에 날리는 끈
        val bandY = bodyTop + body * 0.22f
        drawRect(Pink, Offset(cx - body / 2 + stroke / 2, bandY), Size(body - stroke, body * 0.12f))
        val flutter = if (moving) sin(stride * 2) * body * 0.05f else body * 0.06f
        drawLine(Pink, Offset(cx + body * 0.36f, bandY + body * 0.06f), Offset(cx + body * 0.62f, bandY - body * 0.04f + flutter), strokeWidth = body * 0.07f, cap = StrokeCap.Round)
        drawLine(Pink, Offset(cx + body * 0.36f, bandY + body * 0.06f), Offset(cx + body * 0.6f, bandY + body * 0.12f - flutter), strokeWidth = body * 0.07f, cap = StrokeCap.Round)
        // 등에 붙은 번호표
        val bib = Size(body * 0.42f, body * 0.3f)
        drawRoundRect(Color.White, Offset(cx - bib.width / 2, bodyTop + body * 0.5f), bib, CornerRadius(body * 0.05f))
        drawRoundRect(Ink, Offset(cx - bib.width / 2, bodyTop + body * 0.5f), bib, CornerRadius(body * 0.05f), style = Stroke(stroke * 0.6f))
        drawCircle(Pink, bib.height * 0.22f, Offset(cx, bodyTop + body * 0.5f + bib.height / 2))

        if (stunned) {
            // 머리 위를 도는 별
            val t = (run?.elapsedMs ?: 0L) / 120f
            repeat(3) { i ->
                val angle = t + i * 2.09f
                drawCircle(
                    Sun,
                    body * 0.07f,
                    Offset(cx + cos(angle) * body * 0.4f, bodyTop - body * 0.12f + sin(angle) * body * 0.1f),
                )
            }
        }
    }
}

private fun DrawScope.drawQuad(a: Offset, b: Offset, c: Offset, d: Offset, color: Color) {
    drawPath(
        Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            lineTo(d.x, d.y)
            close()
        },
        color,
    )
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

/** 화면 높이 대비 지평선 위치 */
private const val HORIZON = 0.32f

/** 화면 높이 대비 브레니 발 위치 */
private const val FOOT = 0.9f

/** 카메라가 브레니 뒤에 떨어진 거리(m) */
private const val CAMERA_BACK = 5f

/** 앞으로 보이는 거리(m) */
private const val VIEW_AHEAD = 70f

/** 브레니 자리에서 길 폭의 절반(화면 너비 대비) */
private const val ROAD_HALF = 0.44f

/** 길 높이 대비 문 높이 */
private const val GATE_HEIGHT = 0.85f

private const val STRIPE = 3f
private const val TREE_SPACING = 6f
private const val STRIDE_PER_METER = 2.4f
private const val SPEED_LINE_COMBO = 3

/** 세리머니 동안 앞으로 나아가는 거리(m), 점프 횟수, 다리 움직임 */
private const val FINISH_RUN_METERS = 24f
private const val FINISH_HOPS = 4f
private const val FINISH_STRIDES = 40f
