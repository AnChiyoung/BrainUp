package com.dev.goodluckcy.brainup.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** "모험 지도" 디자인은 밤하늘 배경 하나로 통일하므로 시스템 다크 모드와 관계없이 같은 색을 쓴다. */
private val AdventureColorScheme = darkColorScheme(
    primary = Sun,
    onPrimary = Ink,
    secondary = Mint,
    onSecondary = Ink,
    tertiary = Sky,
    onTertiary = Ink,
    background = Night,
    onBackground = Color.White,
    surface = NightDeep,
    onSurface = Color.White,
    surfaceVariant = NightDeep,
    onSurfaceVariant = Lavender,
    outline = Ink,
    outlineVariant = NightPath,
    error = Danger,
    onError = Color.White,
)

@Composable
fun BrainUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdventureColorScheme,
        typography = Typography,
        content = content,
    )
}
