package com.dev.goodluckcy.brainup.feature.pattern

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.dev.goodluckcy.brainup.core.designsystem.component.RibbonBanner
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Danger
import com.dev.goodluckcy.brainup.core.designsystem.theme.Go
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyGlow
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.feature.game.ContinueWithAdButton
import com.dev.goodluckcy.brainup.feature.game.GamePhase
import com.dev.goodluckcy.brainup.feature.game.GameScaffold
import com.dev.goodluckcy.brainup.feature.game.HeartChip
import com.dev.goodluckcy.brainup.feature.game.PrimaryGameButton
import com.dev.goodluckcy.brainup.feature.game.ProgressDots
import com.dev.goodluckcy.brainup.feature.game.ScoreChip
import com.dev.goodluckcy.brainup.feature.shop.EquippedItemsViewModel
import com.dev.goodluckcy.brainup.core.designsystem.TileSkinStyle
import com.dev.goodluckcy.brainup.core.designsystem.tileSkin
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.ShopItem

@Composable
fun PatternMemoryScreen(
    onBack: () -> Unit,
    onFinish: (GameResult) -> Unit,
    viewModel: PatternMemoryViewModel = hiltViewModel(),
    equippedItems: EquippedItemsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inventory by equippedItems.inventory.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onStart() }

    PatternMemoryContent(
        uiState = uiState,
        tileSkin = inventory.equippedIn(ItemSlot.TILE_SKIN).tileSkin,
        onBack = onBack,
        onStart = viewModel::start,
        onTileTap = viewModel::onTileTap,
        onShowResult = { onFinish(viewModel.result()) },
        onContinue = viewModel::continueAfterReward,
    )
}

@Composable
private fun PatternMemoryContent(
    uiState: PatternMemoryUiState,
    tileSkin: TileSkinStyle,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onTileTap: (Int) -> Unit,
    onShowResult: () -> Unit,
    onContinue: () -> Unit,
) {
    GameScaffold(
        title = stringResource(R.string.game_pattern),
        onBack = onBack,
        skyVariant = 2,
        trailing = {
            ScoreChip(uiState.score)
            HeartChip(if (uiState.continueUsed) 0 else 1)
        },
    ) {
        RibbonBanner(
            text = if (uiState.phase == GamePhase.Ready) {
                stringResource(R.string.ribbon_ready)
            } else {
                stringResource(R.string.ribbon_round_length, uiState.round, uiState.sequence.size)
            },
            color = Sky,
            tailColor = SkyDark,
        )
        Text(
            text = instructionText(uiState),
            style = MaterialTheme.typography.titleLarge,
            color = if (uiState.phase == GamePhase.Finished) Danger else Color.White,
            textAlign = TextAlign.Center,
        )
        if (uiState.sequence.isNotEmpty()) {
            ProgressDots(
                done = if (uiState.phase == GamePhase.Memorizing) 0 else uiState.inputCount,
                total = uiState.sequence.size,
                color = Sky,
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // 작은 화면에서도 위아래 요소와 겹치지 않도록 너비·높이 중 작은 쪽에 맞춘다.
            val side = minOf(maxWidth, maxHeight, MAX_BOARD_SIZE)
            TileBoard(uiState = uiState, skin = tileSkin, onTileTap = onTileTap, modifier = Modifier.size(side))
        }
        when (uiState.phase) {
            GamePhase.Ready -> PrimaryGameButton(stringResource(R.string.action_start), onStart)
            GamePhase.Finished -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ContinueWithAdButton(canContinue = uiState.canContinue, onContinue = onContinue)
                PrimaryGameButton(stringResource(R.string.action_show_result), onShowResult)
            }
            else -> Spacer(Modifier.height(BUTTON_SPACE))
        }
    }
}

@Composable
private fun instructionText(uiState: PatternMemoryUiState): String = when (uiState.phase) {
    GamePhase.Ready -> stringResource(R.string.pattern_rules)
    GamePhase.Memorizing -> stringResource(R.string.pattern_memorizing)
    GamePhase.Answering -> stringResource(R.string.pattern_answering, uiState.inputCount, uiState.sequence.size)
    GamePhase.Success -> stringResource(R.string.pattern_success)
    GamePhase.Finished -> stringResource(R.string.pattern_finished)
}

@Composable
private fun TileBoard(
    uiState: PatternMemoryUiState,
    skin: TileSkinStyle,
    onTileTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = PatternMemoryEngine.GRID_SIZE
    Column(
        modifier = modifier
            .padding(bottom = 8.dp)
            .chunky(
                color = NightDeep,
                shape = RoundedCornerShape(34.dp),
                shadowColor = NightDeeper,
                depth = 8.dp,
                borderWidth = 4.dp,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (row in 0 until size) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (col in 0 until size) {
                    val tile = row * size + col
                    GemTile(
                        tile = tile,
                        uiState = uiState,
                        skin = skin,
                        onClick = { onTileTap(tile) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}

/** 아래쪽이 진한 입체 타일. 점등되면 스킨 색으로 빛난다. */
@Composable
private fun GemTile(
    tile: Int,
    uiState: PatternMemoryUiState,
    skin: TileSkinStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (face, bottom) = when (tile) {
        uiState.wrongTile -> Danger to Color(0xFFB8262D)
        uiState.expectedTile -> skin.lit to skin.litBottom
        uiState.litTile -> skin.lit to skin.litBottom
        uiState.flashedTile -> Go to Mint
        else -> NightLight to Night
    }
    val glowing = tile == uiState.litTile || tile == uiState.expectedTile
    val shape = skin.shape
    val description = stringResource(R.string.pattern_tile, tile + 1)
    Box(
        modifier = modifier
            .drawBehind {
                if (glowing && skin.round) {
                    drawCircle(skin.lit.copy(alpha = 0.45f), radius = size.minDimension / 2 + 7.dp.toPx())
                } else if (glowing) {
                    drawRoundRect(
                        color = skin.lit.copy(alpha = 0.45f),
                        topLeft = Offset(-7.dp.toPx(), -7.dp.toPx()),
                        size = Size(size.width + 14.dp.toPx(), size.height + 14.dp.toPx()),
                        cornerRadius = CornerRadius(28.dp.toPx()),
                    )
                }
            }
            .chunky(color = face, shape = shape, depth = 0.dp)
            .drawBehind {
                // 아래쪽 진한 띠로 보석 같은 입체감(동그란 타일은 생략)
                if (!skin.round) drawRoundRect(
                    color = bottom,
                    topLeft = Offset(3.dp.toPx(), size.height - 11.dp.toPx()),
                    size = Size(size.width - 6.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                )
            }
            .then(if (tile == uiState.expectedTile) Modifier.border(4.dp, Sun, shape) else Modifier)
            .clickable(enabled = uiState.isInputEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = if (skin.round) Alignment.Center else Alignment.TopStart,
    ) {
        if (glowing) {
            GameIcon(
                icon = skin.litIcon,
                size = 22.dp,
                tint = skin.litIconTint,
                fill = skin.litIconFill,
                strokeWidth = 1.4f,
                modifier = if (skin.round) Modifier else Modifier.padding(10.dp),
            )
        }
    }
}

private val MAX_BOARD_SIZE = 420.dp
private val BUTTON_SPACE = 66.dp

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PatternMemoryPlaybackPreview() {
    BrainUpTheme {
        PatternMemoryContent(
            tileSkin = ShopItem.TILE_GEM.tileSkin,
            uiState = PatternMemoryUiState(
                phase = GamePhase.Memorizing,
                round = 2,
                sequence = listOf(0, 4, 8, 2),
                litTile = 4,
                score = 30,
            ),
            onBack = {},
            onStart = {},
            onTileTap = {},
            onShowResult = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PatternMemoryFinishedPreview() {
    BrainUpTheme {
        PatternMemoryContent(
            tileSkin = ShopItem.TILE_GEM.tileSkin,
            uiState = PatternMemoryUiState(
                phase = GamePhase.Finished,
                round = 2,
                sequence = listOf(0, 4, 8, 2),
                inputCount = 2,
                score = 30,
                roundsCleared = 1,
                wrongTile = 6,
                expectedTile = 8,
            ),
            onBack = {},
            onStart = {},
            onTileTap = {},
            onShowResult = {},
            onContinue = {},
        )
    }
}
