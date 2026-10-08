package com.dev.goodluckcy.brainup.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.BannerAd
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.BestRecord
import com.dev.goodluckcy.brainup.domain.model.DailyProgress
import com.dev.goodluckcy.brainup.domain.model.GameType
import java.time.LocalDate

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsContent(uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(uiState: StatsUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_stats), fontWeight = FontWeight.Bold) })
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!uiState.isLoading) {
                StreakCard(uiState)
                SectionTitle(stringResource(R.string.stats_best_records))
                BestRecordsCard(uiState.bestRecords)
                SectionTitle(stringResource(R.string.stats_recent_days))
                RecentDaysCard(uiState.recentDays)
            }
        }
        BannerAd()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun StreakCard(uiState: StatsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_streak_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.stats_streak_value, uiState.streakDays),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when {
                    uiState.isTodayCompleted -> stringResource(R.string.stats_streak_today_done)
                    uiState.streakDays > 0 -> stringResource(R.string.stats_streak_keep, uiState.streakDays + 1)
                    else -> stringResource(R.string.stats_streak_start)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BestRecordsCard(bestRecords: Map<GameType, BestRecord>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            GameType.entries.forEachIndexed { index, gameType ->
                if (index > 0) HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(gameType.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    val record = bestRecords[gameType]
                    Text(
                        text = record?.let { bestRecordText(it) } ?: stringResource(R.string.stats_no_record),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (record != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (record != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun bestRecordText(record: BestRecord): String = when (record.gameType) {
    GameType.NUMBER_MEMORY -> stringResource(R.string.stats_best_number_memory, record.score, record.level)
    GameType.REACTION -> stringResource(R.string.stats_best_reaction, record.score, record.medianReactionMs ?: 0L)
    GameType.PATTERN -> stringResource(R.string.stats_best_pattern, record.score, record.level)
}

@Composable
private fun RecentDaysCard(days: List<DailyProgress>) {
    val maxScore = days.maxOfOrNull { it.totalScore }?.coerceAtLeast(1) ?: 1
    val today = days.lastOrNull()?.date
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                days.forEach { day ->
                    DayBar(
                        day = day,
                        fraction = day.totalScore.toFloat() / maxScore,
                        isToday = day.date == today,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = stringResource(R.string.stats_legend_completed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun DayBar(
    day: DailyProgress,
    fraction: Float,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dayLabels = stringArrayResource(R.array.day_of_week_short)
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (day.totalScore > 0) day.totalScore.toString() else "",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction.coerceIn(MIN_BAR_FRACTION, 1f))
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        when {
                            day.isCompleted -> colors.primary
                            day.totalScore > 0 -> colors.primary.copy(alpha = 0.35f)
                            else -> colors.surfaceVariant
                        },
                    ),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isToday) stringResource(R.string.stats_today) else dayLabels[day.date.dayOfWeek.value - 1],
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private const val MIN_BAR_FRACTION = 0.03f

@Preview(showBackground = true)
@Composable
private fun StatsContentPreview() {
    val today = LocalDate.of(2026, 10, 9)
    BrainUpTheme {
        StatsContent(
            StatsUiState(
                isLoading = false,
                streakDays = 3,
                bestRecords = mapOf(
                    GameType.NUMBER_MEMORY to BestRecord(GameType.NUMBER_MEMORY, 150, 3, null, 0),
                    GameType.REACTION to BestRecord(GameType.REACTION, 712, 1, 288, 0),
                ),
                recentDays = (6 downTo 0).map { offset ->
                    DailyProgress(
                        date = today.minusDays(offset.toLong()),
                        completedGames = if (offset in 1..3) GameType.entries.toSet() else emptySet(),
                        totalScore = listOf(0, 420, 0, 860, 910, 780, 300)[6 - offset],
                    )
                },
            ),
        )
    }
}
