package com.dev.goodluckcy.brainup.feature.result

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.RecordOutcome

@Composable
fun ResultScreen(
    onRetry: (GameType) -> Unit,
    onNextGame: (GameType) -> Unit,
    onHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adServices = LocalAdServices.current
    val activity = LocalActivity.current
    var isLeaving by remember { mutableStateOf(false) }

    // 결과를 확인한 뒤 다음 화면으로 넘어가는 시점에만 전면 광고를 검토한다.
    fun leave(navigate: () -> Unit) {
        if (isLeaving) return
        isLeaving = true
        if (adServices != null && activity != null) {
            adServices.interstitial.showIfEligible(activity, navigate)
        } else {
            navigate()
        }
    }

    BackHandler { leave(onHome) }
    ResultContent(
        uiState = uiState,
        onRetry = { leave { onRetry(uiState.result.gameType) } },
        onNextGame = { gameType -> leave { onNextGame(gameType) } },
        onHome = { leave(onHome) },
    )
}

@Composable
private fun ResultContent(
    uiState: ResultUiState,
    onRetry: () -> Unit,
    onNextGame: (GameType) -> Unit,
    onHome: () -> Unit,
) {
    val result = uiState.result
    val nextGame = GameType.entries[(result.gameType.ordinal + 1) % GameType.entries.size]
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(result.gameType.titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.result_score),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = result.score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                uiState.outcome?.let { PersonalBestText(it) }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    if (result.medianReactionMs != null && result.bestReactionMs != null) {
                        StatItem(stringResource(R.string.reaction_median), stringResource(R.string.unit_ms, result.medianReactionMs))
                        StatItem(stringResource(R.string.reaction_best), stringResource(R.string.unit_ms, result.bestReactionMs))
                    } else {
                        StatItem(stringResource(R.string.result_rounds_cleared), result.roundsCleared.toString())
                        val levelLabel = if (result.gameType == GameType.PATTERN) {
                            R.string.result_max_pattern_length
                        } else {
                            R.string.result_level
                        }
                        StatItem(stringResource(levelLabel), result.level.toString())
                    }
                    StatItem(stringResource(R.string.result_duration), formatDuration(result.durationMs))
                }
            }
            uiState.outcome?.let { DailyChallengeCard(it) }
        }
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        FilledTonalButton(
            onClick = { onNextGame(nextGame) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.action_next_game, stringResource(nextGame.titleRes)),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onHome) {
            Text(stringResource(R.string.action_home))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
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
private fun PersonalBestText(outcome: RecordOutcome) {
    val previousBest = outcome.previousBestScore
    val (text, color) = when {
        outcome.isPersonalBest && previousBest == null ->
            stringResource(R.string.result_first_record) to MaterialTheme.colorScheme.secondary
        outcome.isPersonalBest ->
            stringResource(R.string.result_new_best, previousBest ?: 0) to MaterialTheme.colorScheme.secondary
        else ->
            stringResource(R.string.result_best_score, previousBest ?: 0) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (outcome.isPersonalBest) FontWeight.Bold else FontWeight.Normal,
        color = color,
    )
}

@Composable
private fun DailyChallengeCard(outcome: RecordOutcome) {
    val completed = outcome.todayCompletedCount == GameType.entries.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Text(
            text = when {
                outcome.dailyCompletedNow -> stringResource(R.string.result_daily_completed_now)
                completed -> stringResource(R.string.home_daily_done)
                else -> stringResource(
                    R.string.result_daily_progress,
                    outcome.todayCompletedCount,
                    GameType.entries.size,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Preview(showBackground = true)
@Composable
private fun ResultContentPreview() {
    BrainUpTheme {
        ResultContent(
            uiState = ResultUiState(
                result = GameResult(
                    gameType = GameType.NUMBER_MEMORY,
                    score = 150,
                    level = 3,
                    roundsCleared = 5,
                    durationMs = 74_000,
                ),
                outcome = RecordOutcome(
                    isPersonalBest = true,
                    previousBestScore = 120,
                    dailyCompletedNow = false,
                    todayCompletedCount = 2,
                    todayTotalScore = 870,
                ),
            ),
            onRetry = {},
            onNextGame = {},
            onHome = {},
        )
    }
}
