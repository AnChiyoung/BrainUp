package com.dev.goodluckcy.brainup.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Jua
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun

/** 숫자 카드 한 칸의 상태 */
sealed interface DigitSlot {
    data class Filled(val digit: Int, val color: Color, val textColor: Color = Ink) : DigitSlot
    data object Next : DigitSlot
    data object Empty : DigitSlot
}

/** 자릿수가 많아도 한 줄에 들어가도록 카드 크기를 줄여 그린다. */
@Composable
fun DigitCards(slots: List<DigitSlot>, modifier: Modifier = Modifier, maxCardWidth: Dp = 58.dp) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val gap = 8.dp
        val count = slots.size.coerceAtLeast(1)
        val cardWidth = ((maxWidth - gap * (count - 1)) / count).coerceAtMost(maxCardWidth)
        val cardHeight = cardWidth * 1.28f
        val fontSize = with(LocalDensity.current) { (cardWidth * 0.7f).toSp() }
        val shape = RoundedCornerShape(cardWidth * 0.26f)
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            slots.forEach { slot ->
                val base = Modifier.width(cardWidth)
                when (slot) {
                    is DigitSlot.Filled -> Box(
                        modifier = base
                            .height(cardHeight + 5.dp)
                            .padding(bottom = 5.dp)
                            .chunky(color = slot.color, shape = shape, depth = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(slot.digit.toString(), style = TextStyle(fontFamily = Jua, fontSize = fontSize, color = slot.textColor))
                    }
                    DigitSlot.Next -> Box(
                        base
                            .height(cardHeight + 5.dp)
                            .padding(bottom = 5.dp)
                            .border(3.dp, Sun, shape),
                    )
                    DigitSlot.Empty -> Box(
                        base
                            .height(cardHeight + 5.dp)
                            .padding(bottom = 5.dp)
                            .background(NightDeeper, shape)
                            .border(3.dp, NightLight, shape),
                    )
                }
            }
        }
    }
}
