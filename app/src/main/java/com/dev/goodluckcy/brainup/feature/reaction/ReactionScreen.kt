package com.dev.goodluckcy.brainup.feature.reaction

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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

private val WaitingColor = Color(0xFFC62828)
private val GoColor = Color(0xFF2E7D32)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReactionContent(
    uiState: ReactionUiState,
    onBack: () -> Unit,
    onTap: () -> Unit,
    onShowResult: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.game_reaction)) },
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
            if (uiState.phase == ReactionPhase.Finished) {
                FinishedContent(
                    uiState = uiState,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onShowResult,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(stringResource(R.string.action_show_result), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                TapArea(
                    uiState = uiState,
                    onTap = onTap,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatusRow(uiState: ReactionUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatusItem(
            label = stringResource(R.string.reaction_attempt),
            value = "${uiState.completedAttempts}/${uiState.totalAttempts}",
        )
        StatusItem(
            label = stringResource(R.string.reaction_best),
            value = uiState.reactionsMs.minOrNull()?.let { stringResource(R.string.unit_ms, it) } ?: "-",
        )
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
private fun TapArea(
    uiState: ReactionUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (background, content) = when (uiState.phase) {
        ReactionPhase.Waiting -> WaitingColor to Color.White
        ReactionPhase.Go -> GoColor to Color.White
        ReactionPhase.TooEarly -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }
    val currentOnTap by rememberUpdatedState(onTap)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            // 손가락이 닿는 순간(down)에 측정한다.
            .pointerInput(Unit) { detectTapGestures(onPress = { currentOnTap() }) },
        contentAlignment = Alignment.Center,
    ) {
        val (title, subtitle) = tapAreaText(uiState)
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = content,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = content,
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

@Composable
private fun FinishedContent(
    uiState: ReactionUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.reaction_median),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.unit_ms, uiState.medianMs ?: 0L),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = uiState.reactionsMs.joinToString("  ·  ") { "$it" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (uiState.falseStarts > 0) {
            Text(
                text = stringResource(R.string.reaction_false_starts, uiState.falseStarts),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.reaction_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReactionWaitingPreview() {
    BrainUpTheme {
        ReactionContent(
            uiState = ReactionUiState(phase = ReactionPhase.Waiting, reactionsMs = listOf(312, 287)),
            onBack = {},
            onTap = {},
            onShowResult = {},
        )
    }
}

@Preview(showBackground = true)
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
