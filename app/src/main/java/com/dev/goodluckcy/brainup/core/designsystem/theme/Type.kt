package com.dev.goodluckcy.brainup.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.dev.goodluckcy.brainup.R

/** 제목·숫자·버튼용 둥근 디스플레이 서체 (SIL OFL, docs/licenses/Jua-OFL.txt) */
val Jua = FontFamily(Font(R.font.jua_regular))

private fun TextStyle.jua() = copy(fontFamily = Jua)

/** display·headline·title은 Jua, body·label은 시스템 기본(한국어: Noto Sans CJK) */
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.jua(),
        displayMedium = displayMedium.jua(),
        displaySmall = displaySmall.jua(),
        headlineLarge = headlineLarge.jua(),
        headlineMedium = headlineMedium.jua(),
        headlineSmall = headlineSmall.jua(),
        titleLarge = titleLarge.jua(),
        titleMedium = titleMedium.jua(),
        titleSmall = titleSmall.jua(),
    )
}
