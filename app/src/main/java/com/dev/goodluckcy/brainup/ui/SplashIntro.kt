package com.dev.goodluckcy.brainup.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Brainy
import com.dev.goodluckcy.brainup.core.designsystem.theme.Jua
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 앱 시작 인트로: "BRAIN UP!" 글자가 하나씩 아래에서 튀어 올라
 * 스크롤 끝에서 튕기듯(고무줄처럼) 살짝 넘쳤다가 제자리로 돌아온다.
 */
@Composable
fun SplashIntro(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val finished by rememberUpdatedState(onFinished)
    val letters = remember { LETTERS.map { Animatable(0f) } }
    val mascot = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { mascot.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)) }
        delay(MASCOT_LEAD_MS)
        letters.forEachIndexed { index, letter ->
            launch {
                delay(index * LETTER_STEP_MS)
                // 낮은 감쇠의 스프링: 위로 넘쳐 올라갔다가 몇 번 출렁이며 자리 잡는다.
                letter.animateTo(1f, spring(dampingRatio = 0.38f, stiffness = Spring.StiffnessLow))
            }
        }
        delay(MASCOT_LEAD_MS + letters.size * LETTER_STEP_MS + HOLD_MS)
        finished()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nightSky(variant = 2)
            // 인트로 중에는 아래 지도가 눌리지 않게 한다.
            .clickable(interactionSource = null, indication = null, onClick = {})
            .semantics(mergeDescendants = true) { contentDescription = "BRAIN UP!" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = mascot.value
                    scaleY = mascot.value
                    alpha = mascot.value.coerceIn(0f, 1f)
                }
                .size(112.dp)
                .chunky(color = Brainy, shape = RoundedCornerShape(34.dp), depth = 8.dp, borderColor = Color.White),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.size(80.dp))
        }
        Row(verticalAlignment = Alignment.Bottom) {
            LETTERS.forEachIndexed { index, char ->
                val progress = letters[index].value
                Text(
                    text = char.toString(),
                    modifier = Modifier.graphicsLayer {
                        // 아래에서 위로: progress가 1을 넘으면 제자리보다 위로 튀어 오른다.
                        translationY = (1f - progress) * RISE_DP.dp.toPx()
                        alpha = (progress * 2f).coerceIn(0f, 1f)
                        // 넘쳐 오를 때 살짝 늘어나고, 되돌아올 때 눌린다.
                        val stretch = 1f + (progress - 1f) * 0.6f
                        scaleY = stretch.coerceIn(0.7f, 1.3f)
                        scaleX = (2f - stretch).coerceIn(0.85f, 1.15f)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                    style = TextStyle(
                        fontFamily = Jua,
                        fontSize = 58.sp,
                        color = if (index >= UP_START) Sun else Color.White,
                    ),
                )
                if (index == UP_START - 1) Box(Modifier.size(16.dp))
            }
        }
    }
}

/** 띄어쓰기는 간격 상자로 넣는다. */
private val LETTERS = "BRAINUP!".toList()

/** "UP!"이 시작하는 글자 위치(노란색) */
private const val UP_START = 5

private const val RISE_DP = 90f
private const val MASCOT_LEAD_MS = 250L
private const val LETTER_STEP_MS = 90L
private const val HOLD_MS = 700L
