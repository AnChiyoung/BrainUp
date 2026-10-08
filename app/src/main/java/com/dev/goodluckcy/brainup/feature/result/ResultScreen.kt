package com.dev.goodluckcy.brainup.feature.result

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.designsystem.color
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.RibbonBanner
import com.dev.goodluckcy.brainup.core.designsystem.component.TreasureChest
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.icon
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark
import com.dev.goodluckcy.brainup.core.designsystem.titleRes
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.RecordOutcome
import com.dev.goodluckcy.brainup.feature.game.PrimaryGameButton

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
            .nightSky(variant = 2)
            .confetti()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RibbonBanner(
            text = stringResource(R.string.result_stage_clear),
            color = Sun,
            tailColor = SunDark,
            fontSize = 28,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(result.gameType.color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                GameIcon(icon = result.gameType.icon, size = 18.dp, tint = Ink, strokeWidth = 2.6f)
            }
            Text(stringResource(result.gameType.titleRes), style = MaterialTheme.typography.titleLarge, color = Lavender)
        }
        Text(
            text = result.score.toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5f),
            color = Color.White,
        )
        uiState.outcome?.let { PersonalBestSticker(it) }
        StatChips(result)
        uiState.outcome?.let { ChestProgress(it) }
        Spacer(Modifier.weight(1f))
        PrimaryGameButton(
            text = stringResource(R.string.action_next_stage, stringResource(nextGame.titleRes)),
            onClick = { onNextGame(nextGame) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GameButton(
                onClick = onRetry,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
            ) {
                Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.titleLarge, color = Ink)
            }
            GameButton(
                onClick = onHome,
                color = NightDeep,
                shadowColor = NightDeeper,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
            ) {
                Text(stringResource(R.string.action_to_map), style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }
    }
}

/** 화면 위쪽에 흩뿌린 색종이 */
private fun Modifier.confetti(): Modifier = drawBehind {
    val pieces = listOf(
        Triple(0.08f, 0.10f, Sun), Triple(0.88f, 0.14f, Mint), Triple(0.14f, 0.36f, Pink),
        Triple(0.84f, 0.38f, Sky), Triple(0.30f, 0.06f, Orange), Triple(0.70f, 0.28f, Sun),
    )
    pieces.forEachIndexed { i, (x, y, color) ->
        val s = (if (i % 2 == 0) 12 else 9).dp.toPx()
        drawRoundRect(color, Offset(size.width * x, size.height * y), Size(s, s), CornerRadius(s / 3))
    }
}

@Composable
private fun PersonalBestSticker(outcome: RecordOutcome) {
    if (outcome.isPersonalBest) {
        Text(
            text = stringResource(
                if (outcome.previousBestScore == null) R.string.result_first_sticker else R.string.result_new_best_sticker,
            ),
            modifier = Modifier
                .rotate(-4f)
                .padding(bottom = 3.dp)
                .chunky(color = Pink, shape = RoundedCornerShape(10.dp), depth = 3.dp)
                .padding(horizontal = 14.dp, vertical = 3.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
    } else {
        Text(
            text = stringResource(R.string.result_best_score, outcome.previousBestScore ?: 0),
            style = MaterialTheme.typography.bodyLarge,
            color = Lavender,
        )
    }
}

@Composable
private fun StatChips(result: GameResult) {
    val stats = buildList {
        if (result.medianReactionMs != null && result.bestReactionMs != null) {
            add(stringResource(R.string.reaction_median) to stringResource(R.string.unit_ms, result.medianReactionMs))
            add(stringResource(R.string.reaction_best) to stringResource(R.string.unit_ms, result.bestReactionMs))
        } else {
            add(stringResource(R.string.result_rounds_cleared) to result.roundsCleared.toString())
            val levelLabel = if (result.gameType == GameType.PATTERN) R.string.result_max_pattern_length else R.string.result_level
            add(stringResource(levelLabel) to result.level.toString())
        }
        add(stringResource(R.string.result_duration) to formatDuration(result.durationMs))
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(NightDeep, RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = Lavender)
                Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }
    }
}

/** 보물상자(오늘의 도전) 진행 */
@Composable
private fun ChestProgress(outcome: RecordOutcome) {
    val total = GameType.entries.size
    val completed = outcome.todayCompletedCount == total
    GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TreasureChest(modifier = Modifier.size(width = 50.dp, height = 42.dp), open = completed)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = when {
                        outcome.dailyCompletedNow -> stringResource(R.string.result_chest_opened)
                        completed -> stringResource(R.string.home_all_clear)
                        else -> stringResource(R.string.result_chest_remaining, total - outcome.todayCompletedCount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Night, RoundedCornerShape(999.dp)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(outcome.todayCompletedCount.toFloat() / total)
                            .height(10.dp)
                            .background(Sun, RoundedCornerShape(999.dp)),
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
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
