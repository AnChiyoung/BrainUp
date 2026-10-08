package com.dev.goodluckcy.brainup.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.goodluckcy.brainup.core.designsystem.theme.ChestBody
import com.dev.goodluckcy.brainup.core.designsystem.theme.ChestLid
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Jua
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark

val OutlineWidth = 3.dp

/**
 * 장난감 같은 입체 표면: 아래로 떨어지는 단색 그림자 + 잉크 외곽선 + 바탕색.
 * 그림자는 레이아웃 밖으로 그려지므로 아래쪽에 [depth]만큼 여백을 두고 쓴다.
 */
fun Modifier.chunky(
    color: Color,
    shape: Shape,
    shadowColor: Color = Ink,
    depth: Dp = 6.dp,
    borderColor: Color? = Ink,
    borderWidth: Dp = OutlineWidth,
): Modifier = this
    .drawBehind {
        if (depth > 0.dp) {
            val outline = shape.createOutline(size, layoutDirection, this)
            translate(top = depth.toPx()) { drawOutline(outline, shadowColor) }
        }
    }
    .background(color, shape)
    .then(if (borderColor != null) Modifier.border(borderWidth, borderColor, shape) else Modifier)

/**
 * 누르면 그림자만큼 내려앉는 게임 버튼. [modifier]로 준 높이에 그림자 깊이가 포함된다.
 */
@Composable
fun GameButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Sun,
    shadowColor: Color = Ink,
    shape: Shape = RoundedCornerShape(20.dp),
    depth: Dp = 6.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onClickLabel: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val press by animateDpAsState(
        targetValue = if (pressed && enabled) depth else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "press",
    )
    Row(
        modifier = modifier
            .padding(bottom = depth)
            .offset(y = press)
            .alpha(if (enabled) 1f else 0.45f)
            .chunky(color = color, shape = shape, shadowColor = shadowColor, depth = depth - press)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** 밤하늘 패널 */
@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    color: Color = NightDeep,
    shadowColor: Color = NightDeeper,
    shape: Shape = RoundedCornerShape(24.dp),
    depth: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .padding(bottom = depth)
            .chunky(color = color, shape = shape, shadowColor = shadowColor, depth = depth),
        content = content,
    )
}

/** 양쪽 꼬리가 달린 리본 배너 */
@Composable
fun RibbonBanner(
    text: String,
    color: Color,
    tailColor: Color,
    modifier: Modifier = Modifier,
    fontSize: Int = 24,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(bottom = 4.dp)
                .drawBehind {
                    // 리본 양쪽 꼬리: 라벨 뒤로 비스듬히 아래에 깔린다.
                    val tailW = 40.dp.toPx()
                    val tailTop = 8.dp.toPx()
                    val tailSize = Size(tailW, size.height - 4.dp.toPx())
                    val stroke = OutlineWidth.toPx()
                    listOf(-tailW * 0.7f, size.width - tailW * 0.3f).forEach { x ->
                        drawRect(tailColor, Offset(x, tailTop), tailSize)
                        drawRect(Ink, Offset(x, tailTop), tailSize, style = Stroke(stroke))
                    }
                }
                .chunky(color = color, shape = RoundedCornerShape(12.dp), depth = 4.dp)
                .padding(horizontal = 26.dp, vertical = 6.dp),
            style = TextStyle(fontFamily = Jua, fontSize = fontSize.sp, color = Ink),
        )
    }
}

/** 상단 HUD의 둥근 수치 칩 */
@Composable
fun HudChip(
    text: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .background(NightDeep, RoundedCornerShape(999.dp))
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        leading()
        Text(text = text, style = TextStyle(fontFamily = Jua, fontSize = 16.sp, color = Color.White))
    }
}

/** 코인 모양 점(보상·점수 표시용) */
@Composable
fun CoinDot(size: Dp = 22.dp) {
    Box(
        Modifier
            .size(size)
            .background(Sun, CircleShape)
            .border(2.dp, SunDark, CircleShape),
    )
}

/** 정사각형 아이콘 버튼(뒤로가기·설정) */
@Composable
fun GameIconButton(
    icon: GameIconSpec,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(NightDeep, RoundedCornerShape(14.dp))
            .clickable(onClickLabel = contentDescription, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        GameIcon(icon = icon, size = 22.dp, strokeWidth = 2.8f, contentDescription = contentDescription)
    }
}

/** 보물상자(오늘의 도전 보상) */
@Composable
fun TreasureChest(modifier: Modifier = Modifier, open: Boolean = false) {
    Canvas(modifier = modifier) {
        val stroke = OutlineWidth.toPx()
        val w = size.width
        val h = size.height
        val lidH = h * 0.42f
        val bodyTop = h * 0.36f
        val radius = CornerRadius(w * 0.12f)
        // 몸통
        drawRoundRect(ChestBody, Offset(0f, bodyTop), Size(w, h - bodyTop), radius)
        drawRoundRect(Ink, Offset(0f, bodyTop), Size(w, h - bodyTop), radius, style = Stroke(stroke))
        // 뚜껑 (열리면 위로 들림)
        val lidTop = if (open) -h * 0.18f else 0f
        drawRoundRect(ChestLid, Offset(0f, lidTop), Size(w, lidH), radius)
        drawRoundRect(Ink, Offset(0f, lidTop), Size(w, lidH), radius, style = Stroke(stroke))
        if (open) {
            drawRoundRect(Sun, Offset(w * 0.12f, bodyTop - h * 0.06f), Size(w * 0.76f, h * 0.14f), CornerRadius(8f))
        }
        // 자물쇠
        val lockW = w * 0.22f
        val lockH = h * 0.34f
        val lockOffset = Offset((w - lockW) / 2f, bodyTop - lockH * 0.35f)
        drawRoundRect(Sun, lockOffset, Size(lockW, lockH), CornerRadius(6f))
        drawRoundRect(Ink, lockOffset, Size(lockW, lockH), CornerRadius(6f), style = Stroke(stroke * 0.8f))
    }
}

/** 마스코트 '브레니' 얼굴(작은 아바타) */
@Composable
fun BrainyFace(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val eyeR = w * 0.11f
        drawCircle(Color.White, eyeR, Offset(w * 0.34f, w * 0.44f))
        drawCircle(Color.White, eyeR, Offset(w * 0.66f, w * 0.44f))
        drawCircle(Ink, eyeR * 0.55f, Offset(w * 0.36f, w * 0.47f))
        drawCircle(Ink, eyeR * 0.55f, Offset(w * 0.68f, w * 0.47f))
        drawArc(
            color = Ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.36f, w * 0.48f),
            size = Size(w * 0.28f, w * 0.2f),
            style = Stroke(width = w * 0.06f),
        )
    }
}

/** 밤하늘 배경: 행성 원 두 개와 작은 별. 화면마다 [variant]로 배치를 바꾼다. */
fun Modifier.nightSky(variant: Int = 0): Modifier = this
    .background(Night)
    .drawBehind {
        val w = size.width
        val h = size.height
        val planets = when (variant % 3) {
            0 -> listOf(Offset(w * 1.05f, h * 0.30f) to w * 0.30f, Offset(-w * 0.05f, h * 0.78f) to w * 0.30f)
            1 -> listOf(Offset(w * 0.95f, h * 0.15f) to w * 0.28f, Offset(w * 0.05f, h * 0.85f) to w * 0.32f)
            else -> listOf(Offset(w * 0.5f, h * 0.22f) to w * 0.40f, Offset(w * 0.9f, h * 0.9f) to w * 0.25f)
        }
        planets.forEach { (center, radius) -> drawCircle(NightLight, radius, center) }
        val stars = listOf(0.10f to 0.18f, 0.85f to 0.25f, 0.18f to 0.50f, 0.90f to 0.56f, 0.48f to 0.39f, 0.70f to 0.70f)
        stars.forEachIndexed { i, (x, y) ->
            drawCircle(if (i % 2 == 0) Color.White else Color(0xFFB9AEFF), if (i % 3 == 0) 3.dp.toPx() else 2.dp.toPx(), Offset(w * x, h * y))
        }
    }

