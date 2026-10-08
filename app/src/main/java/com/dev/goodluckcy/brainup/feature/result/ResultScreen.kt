package com.dev.goodluckcy.brainup.feature.result

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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType

// TODO(Day 6~7): 개인 최고 기록 여부 표시
// TODO(Day 8~10): 결과 확인 후 화면 전환 시 전면 광고 조건 검사
@Composable
fun ResultScreen(
    result: GameResult,
    onRetry: () -> Unit,
    onNextGame: (GameType) -> Unit,
    onHome: () -> Unit,
) {
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

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    BrainUpTheme {
        ResultScreen(
            result = GameResult(
                gameType = GameType.NUMBER_MEMORY,
                score = 150,
                level = 3,
                roundsCleared = 5,
                durationMs = 74_000,
            ),
            onRetry = {},
            onNextGame = {},
            onHome = {},
        )
    }
}
