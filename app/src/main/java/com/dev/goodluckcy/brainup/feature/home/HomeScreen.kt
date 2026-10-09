package com.dev.goodluckcy.brainup.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.BannerAd
import com.dev.goodluckcy.brainup.core.designsystem.brainyBackground
import com.dev.goodluckcy.brainup.core.designsystem.color
import com.dev.goodluckcy.brainup.core.designsystem.formatCoins
import com.dev.goodluckcy.brainup.core.designsystem.skyPalette
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.component.CoinDot
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.component.OutlineWidth
import com.dev.goodluckcy.brainup.core.designsystem.component.TreasureChest
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.component.tabBarPadding
import com.dev.goodluckcy.brainup.core.designsystem.icon
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightPath
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.GameType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onGameClick: (GameType) -> Unit,
    onOpenShop: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onGameClick = onGameClick, onOpenChest = viewModel::openChest)
    HomeDialogHost(
        uiState = uiState,
        onClaimChest = viewModel::claimChest,
        onDismiss = viewModel::dismissDialog,
        onOpenShop = onOpenShop,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onGameClick: (GameType) -> Unit,
    onOpenChest: () -> Unit,
) {
    // 아직 오늘 안 한 첫 게임이 '다음 스테이지'. 모두 끝냈으면 처음 게임을 한 판 더.
    val nextGame = GameType.entries.firstOrNull { it !in uiState.completedGames }
    val sky = uiState.mapTheme.skyPalette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky(variant = 0, ground = sky.ground, planet = sky.planet, accentPlanet = sky.accentPlanet)
            .statusBarsPadding()
            .tabBarPadding(),
    ) {
        HomeHud(uiState)
        AdventureMap(
            uiState = uiState,
            nextGame = nextGame,
            onGameClick = onGameClick,
            onOpenChest = onOpenChest,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        GameButton(
            onClick = {
                if (uiState.isChestReady) onOpenChest() else onGameClick(nextGame ?: GameType.entries.first())
            },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(70.dp),
            depth = 7.dp,
            shape = RoundedCornerShape(22.dp),
        ) {
            if (uiState.isChestReady) {
                TreasureChest(modifier = Modifier.size(width = 34.dp, height = 28.dp), open = true)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.home_open_chest),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                )
            } else {
                PlayButtonLabel(nextGame)
            }
        }
        BannerAd(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun PlayButtonLabel(nextGame: GameType?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GameIcon(icon = GameIcons.Play, size = 26.dp, tint = Ink, fill = Ink, strokeWidth = 1f)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(if (nextGame != null) R.string.home_play else R.string.home_play_again),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
            )
            Text(
                text = if (nextGame != null) {
                    stringResource(R.string.home_play_next, stringResource(nextGame.titleRes))
                } else {
                    stringResource(R.string.home_all_clear)
                },
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
            )
        }
    }
}

@Composable
private fun HomeHud(uiState: HomeUiState) {
    val dateText = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    }
    val coinDesc = stringResource(R.string.coin_balance_desc, formatCoins(uiState.coins))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .brainyBackground(uiState.brainy, RoundedCornerShape(16.dp))
                .border(OutlineWidth, Color.White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.size(34.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = dateText, style = MaterialTheme.typography.labelMedium, color = Lavender)
            Text(
                text = stringResource(R.string.home_adventure_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
        }
        HudChip(
            text = formatCoins(uiState.coins),
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = coinDesc
            },
        ) { CoinDot(size = 20.dp) }
        HudChip(text = uiState.streakDays.toString()) {
            GameIcon(icon = GameIcons.Flame, size = 18.dp, tint = Orange, fill = Orange, strokeWidth = 1.5f)
        }
        if (uiState.shields > 0) {
            val shieldDesc = stringResource(R.string.shield_count_desc, uiState.shields)
            HudChip(
                text = uiState.shields.toString(),
                modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = shieldDesc },
            ) { ShieldChipIcon() }
        }
    }
}

/** 지도 위 노드 위치(지도 영역 대비 비율). 아래에서 위로 숫자 → 반응 → 패턴 → 보물상자 */
private val NodePositions = mapOf(
    GameType.NUMBER_MEMORY to Offset(0.28f, 0.83f),
    GameType.REACTION to Offset(0.70f, 0.58f),
    GameType.PATTERN to Offset(0.30f, 0.33f),
)
private val ChestPosition = Offset(0.70f, 0.10f)

@Composable
private fun AdventureMap(
    uiState: HomeUiState,
    nextGame: GameType?,
    onGameClick: (GameType) -> Unit,
    onOpenChest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight
        // 작은 화면에서도 노드가 겹치지 않도록 지도 높이에 맞춰 크기를 줄인다.
        val nodeSize = minOf(92.dp, mapHeight * 0.22f)

        // 진입 연출: 길이 아래에서 위로 그려지고, 스테이지가 차례로 통통 튀어나온다.
        val pathProgress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            pathProgress.animateTo(1f, tween(durationMillis = PATH_DRAW_MS, easing = FastOutSlowInEasing))
        }
        MapPath(progress = pathProgress.value, modifier = Modifier.fillMaxSize())

        val chestReady = uiState.isChestReady
        val chestAppear = rememberPopIn(delayMs = CHEST_APPEAR_MS)
        val chestHop = if (chestReady) rememberIdleHop() else 0f
        val chestLabel = stringResource(R.string.home_open_chest)
        Column(
            modifier = Modifier
                .offset(
                    x = mapWidth * ChestPosition.x - 70.dp,
                    // "열기!" 말풍선이 붙어도 상자 자리는 그대로 둔다.
                    y = mapHeight * ChestPosition.y - nodeSize * 0.35f - if (chestReady) ChestBubbleSpace else 0.dp,
                )
                .width(140.dp)
                .popInLayer(chestAppear)
                .clickable(
                    enabled = chestReady,
                    interactionSource = null,
                    indication = null,
                    onClickLabel = chestLabel,
                    role = Role.Button,
                    onClick = onOpenChest,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (chestReady) {
                Text(
                    text = stringResource(R.string.chest_open_bubble),
                    modifier = Modifier
                        .graphicsLayer { translationY = chestHop.dp.toPx() }
                        .padding(bottom = 3.dp)
                        .chunky(color = Color.White, shape = RoundedCornerShape(10.dp), depth = 3.dp, borderWidth = 2.dp)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
            }
            TreasureChest(
                modifier = Modifier
                    .graphicsLayer { translationY = chestHop.dp.toPx() }
                    .drawBehind {
                        if (chestReady) {
                            drawCircle(Sun.copy(alpha = 0.35f), radius = size.maxDimension * 0.75f)
                            drawCircle(Sun.copy(alpha = 0.25f), radius = size.maxDimension * 0.95f)
                        }
                    }
                    .size(width = nodeSize, height = nodeSize * 0.75f),
                open = uiState.chestClaimed,
            )
            Text(
                text = stringResource(R.string.home_chest, uiState.completedCount, uiState.totalCount),
                modifier = Modifier
                    .background(Sun, RoundedCornerShape(999.dp))
                    .border(2.dp, Ink, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
            )
        }

        GameType.entries.forEach { gameType ->
            val position = NodePositions.getValue(gameType)
            MapNode(
                gameType = gameType,
                done = gameType in uiState.completedGames,
                isNext = gameType == nextGame,
                nodeSize = nodeSize,
                appearDelayMs = NODE_APPEAR_START_MS + gameType.ordinal * NODE_APPEAR_STEP_MS,
                onClick = { onGameClick(gameType) },
                modifier = Modifier.offset(
                    x = mapWidth * position.x - NodeColumnWidth / 2,
                    y = mapHeight * position.y - nodeSize / 2 - BubbleSpace,
                ),
            )
        }
    }
}

private val NodeColumnWidth = 150.dp
private val ChestBubbleSpace = 30.dp
private val BubbleSpace = 36.dp

@Composable
private fun MapPath(progress: Float, modifier: Modifier) {
    Canvas(modifier = modifier) {
        fun at(p: Offset) = Offset(size.width * p.x, size.height * p.y)
        val number = at(NodePositions.getValue(GameType.NUMBER_MEMORY))
        val reaction = at(NodePositions.getValue(GameType.REACTION))
        val pattern = at(NodePositions.getValue(GameType.PATTERN))
        val chest = at(ChestPosition)
        val path = Path().apply {
            moveTo(number.x, number.y)
            cubicTo(number.x, number.y - 90f, reaction.x, reaction.y + 120f, reaction.x, reaction.y)
            cubicTo(reaction.x, reaction.y - 120f, pattern.x, pattern.y + 120f, pattern.x, pattern.y)
            cubicTo(pattern.x, pattern.y - 120f, chest.x, chest.y + 120f, chest.x, chest.y)
        }.let { full ->
            // 진행률만큼만 잘라 그린다.
            val measure = PathMeasure().apply { setPath(full, false) }
            Path().also { measure.getSegment(0f, measure.length * progress, it, true) }
        }
        drawPath(path, NightPath, style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round))
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.55f),
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 14.dp.toPx())),
            ),
        )
    }
}

@Composable
private fun MapNode(
    gameType: GameType,
    done: Boolean,
    isNext: Boolean,
    nodeSize: Dp,
    appearDelayMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(gameType.titleRes)
    val appear = rememberPopIn(delayMs = appearDelayMs)
    // 다음 스테이지는 계속 통통 튀어 눈에 띄게 한다.
    val hop = if (isNext) rememberIdleHop() else 0f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    Column(
        modifier = modifier
            .width(NodeColumnWidth)
            .popInLayer(appear)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = title,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_next_stage),
            modifier = Modifier
                .graphicsLayer { translationY = hop.dp.toPx() }
                .alpha(if (isNext) 1f else 0f)
                .padding(bottom = 6.dp)
                .chunky(color = Color.White, shape = RoundedCornerShape(10.dp), depth = 3.dp, borderWidth = 2.dp)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Box(
            modifier = Modifier.graphicsLayer {
                translationY = hop.dp.toPx()
                scaleX = pressScale
                scaleY = pressScale
            },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .drawBehind {
                        if (isNext) drawCircle(gameType.color.copy(alpha = 0.3f), radius = size.minDimension / 2 + 10.dp.toPx())
                    }
                    .chunky(color = if (done) Sun else Color.White, shape = CircleShape, depth = 7.dp)
                    .padding(5.dp)
                    .background(gameType.color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                GameIcon(icon = gameType.icon, size = nodeSize * 0.44f, tint = Ink, contentDescription = title)
            }
            if (done) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .background(Sun, CircleShape)
                        .border(OutlineWidth, Ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    GameIcon(icon = GameIcons.Check, size = 16.dp, tint = Ink, strokeWidth = 3.4f)
                }
            }
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

/** 지연 후 0 → 1로 튀어 오르는 등장 값(스프링이라 1을 살짝 넘었다 돌아온다) */
@Composable
private fun rememberPopIn(delayMs: Long): Float {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs)
        appear.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMediumLow))
    }
    return appear.value
}

private fun Modifier.popInLayer(appear: Float): Modifier = graphicsLayer {
    scaleX = appear
    scaleY = appear
    alpha = appear.coerceIn(0f, 1f)
    transformOrigin = TransformOrigin(0.5f, 0.65f)
}

/** 위로 톡 뛰었다가 한 번 더 작게 튀고 잠시 쉬는 반복 움직임(dp) */
@Composable
private fun rememberIdleHop(): Float {
    val transition = rememberInfiniteTransition(label = "idleHop")
    val hop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1400
                0f at 0
                -12f at 220 using FastOutSlowInEasing
                0f at 440 using FastOutLinearInEasing
                -4f at 560 using FastOutSlowInEasing
                0f at 680 using FastOutLinearInEasing
                0f at 1400
            },
        ),
        label = "hop",
    )
    return hop
}

private const val PATH_DRAW_MS = 650
private const val NODE_APPEAR_START_MS = 300L
private const val NODE_APPEAR_STEP_MS = 150L
private const val CHEST_APPEAR_MS = 800L

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun HomeContentPreview() {
    BrainUpTheme {
        HomeContent(
            uiState = HomeUiState(
                completedGames = setOf(GameType.NUMBER_MEMORY, GameType.REACTION),
                todayScore = 712,
                streakDays = 4,
            ),
            onGameClick = {},
            onOpenChest = {},
        )
    }
}
