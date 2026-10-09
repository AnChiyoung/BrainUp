package com.dev.goodluckcy.brainup.feature.numbermemory

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.RibbonBanner
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Danger
import com.dev.goodluckcy.brainup.core.designsystem.theme.Go
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.OrangeDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.feature.game.ContinueWithAdButton
import com.dev.goodluckcy.brainup.feature.game.DigitCards
import com.dev.goodluckcy.brainup.feature.game.DigitSlot
import com.dev.goodluckcy.brainup.feature.game.GamePhase
import com.dev.goodluckcy.brainup.feature.game.GameScaffold
import com.dev.goodluckcy.brainup.feature.game.HeartChip
import com.dev.goodluckcy.brainup.feature.game.PrimaryGameButton
import com.dev.goodluckcy.brainup.feature.game.ScoreChip

@Composable
fun NumberMemoryScreen(
    onBack: () -> Unit,
    onFinish: (GameResult) -> Unit,
    viewModel: NumberMemoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onStart() }

    NumberMemoryContent(
        uiState = uiState,
        onBack = onBack,
        onStart = viewModel::start,
        onDigit = viewModel::onDigit,
        onDelete = viewModel::onDelete,
        onShowResult = { onFinish(viewModel.result()) },
        onContinue = viewModel::continueAfterReward,
    )
}

@Composable
private fun NumberMemoryContent(
    uiState: NumberMemoryUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onShowResult: () -> Unit,
    onContinue: () -> Unit,
) {
    GameScaffold(
        title = stringResource(R.string.game_number_memory),
        onBack = onBack,
        trailing = {
            ScoreChip(uiState.score)
            HeartChip(if (uiState.continueUsed) 0 else 1)
        },
    ) {
        RibbonBanner(
            text = if (uiState.phase == GamePhase.Ready) {
                stringResource(R.string.ribbon_ready)
            } else {
                stringResource(R.string.ribbon_round_level, uiState.round, uiState.level)
            },
            color = Orange,
            tailColor = OrangeDark,
        )
        GamePanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(instructionRes(uiState.phase)),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (uiState.phase == GamePhase.Finished) Danger else Color.White,
                    textAlign = TextAlign.Center,
                )
                when (uiState.phase) {
                    GamePhase.Ready -> Text(
                        text = stringResource(R.string.number_memory_rules),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Lavender,
                        textAlign = TextAlign.Center,
                    )
                    GamePhase.Memorizing -> MemorizingContent(uiState)
                    GamePhase.Answering -> DigitCards(answerSlots(uiState.input, uiState.sequence.size))
                    GamePhase.Success -> Text(
                        text = stringResource(R.string.number_memory_correct),
                        style = MaterialTheme.typography.displayMedium,
                        color = Go,
                    )
                    GamePhase.Finished -> FinishedContent(uiState)
                }
            }
        }
        when (uiState.phase) {
            GamePhase.Ready -> PrimaryGameButton(stringResource(R.string.action_start), onStart)
            GamePhase.Finished -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ContinueWithAdButton(canContinue = uiState.canContinue, onContinue = onContinue)
                PrimaryGameButton(stringResource(R.string.action_show_result), onShowResult)
            }
            else -> NumberPad(
                enabled = uiState.isInputEnabled,
                onDigit = onDigit,
                onDelete = onDelete,
            )
        }
    }
}

private fun instructionRes(phase: GamePhase): Int = when (phase) {
    GamePhase.Ready -> R.string.number_memory_ready
    GamePhase.Memorizing -> R.string.number_memory_memorizing
    GamePhase.Answering -> R.string.number_memory_answering
    GamePhase.Success -> R.string.number_memory_success
    GamePhase.Finished -> R.string.number_memory_finished
}

private fun answerSlots(input: List<Int>, length: Int): List<DigitSlot> = List(length) { i ->
    when {
        i < input.size -> DigitSlot.Filled(input[i], Orange)
        i == input.size -> DigitSlot.Next
        else -> DigitSlot.Empty
    }
}

@Composable
private fun MemorizingContent(uiState: NumberMemoryUiState) {
    val remaining = remember(uiState.memorizeToken) { Animatable(1f) }
    LaunchedEffect(uiState.memorizeToken) {
        remaining.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = uiState.memorizeDurationMs.toInt(),
                easing = LinearEasing,
            ),
        )
    }
    DigitCards(uiState.sequence.map { DigitSlot.Filled(it, Color.White) })
    // 남은 암기 시간
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(12.dp)
            .background(NightDeeper, RoundedCornerShape(999.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(remaining.value)
                .height(12.dp)
                .background(Sun, RoundedCornerShape(999.dp)),
        )
    }
}

@Composable
private fun FinishedContent(uiState: NumberMemoryUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.number_memory_answer), style = MaterialTheme.typography.labelLarge, color = Lavender)
        DigitCards(uiState.sequence.map { DigitSlot.Filled(it, Color.White) }, maxCardWidth = 46.dp)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.number_memory_your_input), style = MaterialTheme.typography.labelLarge, color = Lavender)
        DigitCards(
            uiState.input.mapIndexed { i, digit ->
                val correct = digit == uiState.sequence.getOrNull(i)
                DigitSlot.Filled(digit, if (correct) Orange else Danger, if (correct) Ink else Color.White)
            },
            maxCardWidth = 46.dp,
        )
    }
}

@Composable
private fun NumberPad(
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { digit ->
                    DigitKey(digit, enabled, Modifier.weight(1f)) { onDigit(digit) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(Modifier.weight(1f))
            DigitKey(0, enabled, Modifier.weight(1f)) { onDigit(0) }
            GameButton(
                onClick = onDelete,
                enabled = enabled,
                color = Orange,
                modifier = Modifier
                    .weight(1f)
                    .height(KEY_HEIGHT),
                onClickLabel = stringResource(R.string.action_delete),
            ) {
                GameIcon(
                    icon = GameIcons.Backspace,
                    size = 28.dp,
                    tint = Ink,
                    strokeWidth = 2.6f,
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        }
    }
}

@Composable
private fun DigitKey(
    digit: Int,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    GameButton(
        onClick = onClick,
        enabled = enabled,
        color = Color.White,
        modifier = modifier.height(KEY_HEIGHT),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text = digit.toString(), style = MaterialTheme.typography.headlineMedium, color = Ink)
    }
}

private val KEY_HEIGHT = 66.dp

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NumberMemoryAnsweringPreview() {
    BrainUpTheme {
        NumberMemoryContent(
            uiState = NumberMemoryUiState(
                phase = GamePhase.Answering,
                round = 3,
                level = 2,
                sequence = listOf(4, 8, 1, 5),
                input = listOf(4, 8),
                score = 60,
            ),
            onBack = {},
            onStart = {},
            onDigit = {},
            onDelete = {},
            onShowResult = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NumberMemoryFinishedPreview() {
    BrainUpTheme {
        NumberMemoryContent(
            uiState = NumberMemoryUiState(
                phase = GamePhase.Finished,
                round = 3,
                level = 2,
                sequence = listOf(4, 8, 1, 5),
                input = listOf(4, 8, 5, 1),
                score = 60,
                roundsCleared = 2,
            ),
            onBack = {},
            onStart = {},
            onDigit = {},
            onDelete = {},
            onShowResult = {},
            onContinue = {},
        )
    }
}
