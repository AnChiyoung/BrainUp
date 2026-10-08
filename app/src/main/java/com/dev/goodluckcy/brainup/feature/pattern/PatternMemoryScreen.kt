package com.dev.goodluckcy.brainup.feature.pattern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.feature.game.ContinueWithAdButton
import com.dev.goodluckcy.brainup.feature.game.GamePhase

@Composable
fun PatternMemoryScreen(
    onBack: () -> Unit,
    onFinish: (GameResult) -> Unit,
    viewModel: PatternMemoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onStart() }

    PatternMemoryContent(
        uiState = uiState,
        onBack = onBack,
        onStart = viewModel::start,
        onTileTap = viewModel::onTileTap,
        onShowResult = { onFinish(viewModel.result()) },
        onContinue = viewModel::continueAfterReward,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatternMemoryContent(
    uiState: PatternMemoryUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onTileTap: (Int) -> Unit,
    onShowResult: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.game_pattern)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatusRow(uiState)
            Spacer(Modifier.height(16.dp))
            Text(
                text = instructionText(uiState),
                style = MaterialTheme.typography.titleMedium,
                color = if (uiState.phase == GamePhase.Finished) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TileGrid(uiState = uiState, onTileTap = onTileTap)
            }
            when (uiState.phase) {
                GamePhase.Ready -> PrimaryButton(stringResource(R.string.action_start), onStart)
                GamePhase.Finished -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContinueWithAdButton(canContinue = uiState.canContinue, onContinue = onContinue)
                    PrimaryButton(stringResource(R.string.action_show_result), onShowResult)
                }
                else -> Spacer(Modifier.height(BUTTON_HEIGHT))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun instructionText(uiState: PatternMemoryUiState): String = when (uiState.phase) {
    GamePhase.Ready -> stringResource(R.string.pattern_rules)
    GamePhase.Memorizing -> stringResource(R.string.pattern_memorizing)
    GamePhase.Answering -> stringResource(
        R.string.pattern_answering,
        uiState.inputCount,
        uiState.sequence.size,
    )
    GamePhase.Success -> stringResource(R.string.pattern_success)
    GamePhase.Finished -> stringResource(R.string.pattern_finished)
}

@Composable
private fun StatusRow(uiState: PatternMemoryUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatusItem(stringResource(R.string.label_round), uiState.round.toString())
        StatusItem(
            stringResource(R.string.pattern_length),
            if (uiState.sequence.isEmpty()) "-" else uiState.sequence.size.toString(),
        )
        StatusItem(stringResource(R.string.label_score), uiState.score.toString())
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TileGrid(
    uiState: PatternMemoryUiState,
    onTileTap: (Int) -> Unit,
) {
    val size = PatternMemoryEngine.GRID_SIZE
    Column(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (row in 0 until size) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (col in 0 until size) {
                    val tile = row * size + col
                    Tile(
                        tile = tile,
                        uiState = uiState,
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

@Composable
private fun Tile(
    tile: Int,
    uiState: PatternMemoryUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val background: Color = when (tile) {
        uiState.wrongTile -> colors.error
        uiState.expectedTile -> colors.primary
        uiState.litTile -> colors.primary
        uiState.flashedTile -> colors.secondary
        else -> colors.surfaceVariant
    }
    val shape = RoundedCornerShape(16.dp)
    val description = stringResource(R.string.pattern_tile, tile + 1)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(
                if (tile == uiState.expectedTile) {
                    Modifier.border(4.dp, colors.onPrimaryContainer, shape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = uiState.isInputEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    )
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(BUTTON_HEIGHT),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

private val BUTTON_HEIGHT = 56.dp

@Preview(showBackground = true)
@Composable
private fun PatternMemoryPlaybackPreview() {
    BrainUpTheme {
        PatternMemoryContent(
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

@Preview(showBackground = true)
@Composable
private fun PatternMemoryFinishedPreview() {
    BrainUpTheme {
        PatternMemoryContent(
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
