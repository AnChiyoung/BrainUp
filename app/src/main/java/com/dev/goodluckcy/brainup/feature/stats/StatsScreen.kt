package com.dev.goodluckcy.brainup.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.BannerAd
import com.dev.goodluckcy.brainup.core.designsystem.color
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.component.tabBarPadding
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.ChestLid
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightPath
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
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

@Composable
private fun StatsContent(uiState: StatsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky(variant = 1)
            .statusBarsPadding()
            .tabBarPadding(),
    ) {
        Text(
            text = stringResource(R.string.trophy_room_title),
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!uiState.isLoading) {
                StreakBanner(uiState)
                WeekStamps(uiState.recentDays)
                Text(
                    text = stringResource(R.string.trophy_shelf_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                TrophyShelf(uiState.bestRecords)
            }
        }
        BannerAd(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun StreakBanner(uiState: StatsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .chunky(color = Orange, shape = RoundedCornerShape(26.dp), depth = 6.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GameIcon(icon = GameIcons.Flame, size = 60.dp, tint = Ink, fill = Sun, strokeWidth = 1.4f)
        Column {
            Text(
                text = stringResource(R.string.trophy_streak, uiState.streakDays),
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Text(
                text = when {
                    uiState.isTodayCompleted -> stringResource(R.string.stats_streak_today_done)
                    uiState.streakDays > 0 -> stringResource(R.string.stats_streak_keep, uiState.streakDays + 1)
                    else -> stringResource(R.string.stats_streak_start)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
        }
    }
}

/** 최근 7일 도장판: 오늘의 도전을 완료한 날은 금색 도장 */
@Composable
private fun WeekStamps(days: List<DailyProgress>) {
    val dayLabels = stringArrayResource(R.array.day_of_week_short)
    val today = days.lastOrNull()?.date
    val total = GameType.entries.size
    GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.trophy_week_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.trophy_chests, days.count { it.isCompleted }),
                    style = MaterialTheme.typography.labelMedium,
                    color = Lavender,
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    val isToday = day.date == today
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Stamp(day = day, isToday = isToday, total = total)
                        Text(
                            text = if (isToday) stringResource(R.string.stats_today) else dayLabels[day.date.dayOfWeek.value - 1],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) Sun else Lavender,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stamp(day: DailyProgress, isToday: Boolean, total: Int) {
    val size = 38.dp
    when {
        day.isCompleted -> Box(
            Modifier
                .size(size)
                .background(Sun, CircleShape)
                .border(3.dp, Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            GameIcon(icon = GameIcons.Check, size = 18.dp, tint = Ink, strokeWidth = 3.4f)
        }
        // 불꽃 방패로 지킨 날
        day.shielded -> Box(
            Modifier
                .size(size)
                .background(Sky, CircleShape)
                .border(3.dp, Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            GameIcon(
                icon = GameIcons.Shield,
                size = 20.dp,
                tint = Ink,
                fill = Color.White,
                strokeWidth = 2f,
                contentDescription = stringResource(R.string.item_shield),
            )
        }
        isToday -> Box(
            Modifier
                .size(size)
                .drawBehind { drawCircle(Sun.copy(alpha = 0.4f), this.size.minDimension / 2 + 4.dp.toPx()) }
                .background(Color.White, CircleShape)
                .border(3.dp, Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${day.completedGames.size}/$total",
                style = MaterialTheme.typography.labelLarge,
                color = Ink,
            )
        }
        else -> Box(
            Modifier
                .size(size)
                .drawBehind {
                    drawCircle(NightDeeper)
                    drawCircle(
                        color = NightPath,
                        radius = this.size.minDimension / 2 - 1.5.dp.toPx(),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                        ),
                    )
                },
        )
    }
}

/** 게임별 최고 기록 트로피 선반 */
@Composable
private fun TrophyShelf(bestRecords: Map<GameType, BestRecord>) {
    GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp) {
        Column {
            Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 10.dp)) {
                GameType.entries.forEach { gameType ->
                    val record = bestRecords[gameType]
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (record != null) 1f else 0.55f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        GameIcon(
                            icon = GameIcons.Trophy,
                            size = 58.dp,
                            tint = if (record != null) Ink else NightPath,
                            fill = if (record != null) gameType.color else NightDeeper,
                            strokeWidth = 1.4f,
                        )
                        Text(
                            text = record?.let { trophyValue(it) } ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(gameType.titleRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = Lavender,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            // 나무 선반
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(ChestLid, RoundedCornerShape(bottomStart = 21.dp, bottomEnd = 21.dp)),
            )
        }
    }
}

@Composable
private fun trophyValue(record: BestRecord): String = when (record.gameType) {
    GameType.REACTION -> stringResource(R.string.unit_ms, record.medianReactionMs ?: 0L)
    else -> stringResource(R.string.trophy_points, record.score)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun StatsContentPreview() {
    val today = LocalDate.of(2026, 10, 9)
    BrainUpTheme {
        StatsContent(
            StatsUiState(
                isLoading = false,
                streakDays = 4,
                bestRecords = mapOf(
                    GameType.NUMBER_MEMORY to BestRecord(GameType.NUMBER_MEMORY, 150, 3, null, 0),
                    GameType.REACTION to BestRecord(GameType.REACTION, 712, 1, 288, 0),
                ),
                recentDays = (6 downTo 0).map { offset ->
                    DailyProgress(
                        date = today.minusDays(offset.toLong()),
                        completedGames = when (offset) {
                            0 -> setOf(GameType.NUMBER_MEMORY, GameType.REACTION)
                            in 1..4 -> GameType.entries.toSet()
                            else -> emptySet()
                        },
                    )
                },
            ),
        )
    }
}
