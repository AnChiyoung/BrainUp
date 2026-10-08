package com.dev.goodluckcy.brainup.feature.numbermemory

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.feature.game.GamePhase

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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberMemoryContent(
    uiState: NumberMemoryUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onShowResult: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.game_number_memory)) },
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
            if (uiState.phase != GamePhase.Ready) {
                StatusRow(uiState)
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = stringResource(instructionRes(uiState.phase)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (uiState.phase) {
                    GamePhase.Ready -> ReadyContent()
                    GamePhase.Memorizing -> MemorizingContent(uiState)
                    GamePhase.Answering -> InputSlots(uiState.input, uiState.sequence.size)
                    GamePhase.Success -> FeedbackText(
                        text = stringResource(R.string.number_memory_correct),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    GamePhase.Finished -> FinishedContent(uiState)
                }
            }
            when (uiState.phase) {
                GamePhase.Ready -> PrimaryButton(stringResource(R.string.action_start), onStart)
                GamePhase.Finished -> PrimaryButton(stringResource(R.string.action_show_result), onShowResult)
                else -> NumberPad(
                    enabled = uiState.isInputEnabled,
                    onDigit = onDigit,
                    onDelete = onDelete,
                )
            }
            Spacer(Modifier.height(8.dp))
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

@Composable
private fun StatusRow(uiState: NumberMemoryUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatusItem(stringResource(R.string.label_round), uiState.round.toString())
        StatusItem(stringResource(R.string.label_level), uiState.level.toString())
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
private fun ReadyContent() {
    Text(
        text = stringResource(R.string.number_memory_rules),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = uiState.sequence.joinToString(""),
            fontSize = digitFontSize(uiState.sequence.size),
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        LinearProgressIndicator(
            progress = { remaining.value },
            modifier = Modifier.fillMaxWidth(0.6f),
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun InputSlots(input: List<Int>, length: Int) {
    val text = buildString {
        for (i in 0 until length) append(input.getOrNull(i)?.toString() ?: "_")
    }
    Text(
        text = text,
        fontSize = digitFontSize(length),
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.sp,
    )
}

@Composable
private fun FeedbackText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Composable
private fun FinishedContent(uiState: NumberMemoryUiState) {
    val errorColor = MaterialTheme.colorScheme.error
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FeedbackText(
            text = stringResource(R.string.number_memory_wrong),
            color = errorColor,
        )
        AnswerLine(
            label = stringResource(R.string.number_memory_answer),
            text = AnnotatedString(uiState.sequence.joinToString("")),
        )
        AnswerLine(
            label = stringResource(R.string.number_memory_your_input),
            text = buildAnnotatedString {
                uiState.input.forEachIndexed { i, digit ->
                    if (digit == uiState.sequence.getOrNull(i)) {
                        append(digit.toString())
                    } else {
                        withStyle(SpanStyle(color = errorColor)) { append(digit.toString()) }
                    }
                }
            },
        )
    }
}

@Composable
private fun AnswerLine(label: String, text: AnnotatedString) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun NumberPad(
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    DigitKey(digit.toString(), enabled, Modifier.weight(1f)) { onDigit(digit) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.weight(1f))
            DigitKey("0", enabled, Modifier.weight(1f)) { onDigit(0) }
            TextButton(
                onClick = onDelete,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(KEY_HEIGHT),
            ) {
                Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun DigitKey(
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(KEY_HEIGHT),
    ) {
        Text(text = label, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val KEY_HEIGHT = 60.dp

private fun digitFontSize(length: Int): TextUnit = when {
    length <= 6 -> 56.sp
    length <= 9 -> 40.sp
    else -> 30.sp
}

@Preview(showBackground = true)
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
        )
    }
}

@Preview(showBackground = true)
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
        )
    }
}
