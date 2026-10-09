package com.dev.goodluckcy.brainup.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 24×24 기준 SVG 경로로 그린 아이콘. 디자인 시안의 선 아이콘을 그대로 옮긴다. */
class GameIconSpec(vararg val paths: String)

object GameIcons {
    val Hash = GameIconSpec("M4 9h16", "M4 15h16", "M10 3L8 21", "M16 3l-2 18")
    val Bolt = GameIconSpec("M13 2L4 14h7l-1 8 9-12h-7z")
    val Grid = GameIconSpec(
        "M5 3h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
        "M16 3h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
        "M5 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
        "M16 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
    )
    val Flame = GameIconSpec("M12 2c2 4 6 6 6 11a6 6 0 0 1-12 0c0-3 2-5 3-7 1 2 2 3 3 3 0-3-1-5 0-7z")
    val Heart = GameIconSpec("M12 21s-8-5-8-11a4.5 4.5 0 0 1 8-2.8A4.5 4.5 0 0 1 20 10c0 6-8 11-8 11z")
    val Star = GameIconSpec("M12 2l3 6.5 7 .8-5.2 4.8 1.5 7-6.3-3.6-6.3 3.6 1.5-7L2 9.3l7-.8z")
    val Trophy = GameIconSpec(
        "M8 21h8", "M12 17v4", "M7 4h10v5a5 5 0 0 1-10 0z",
        "M17 5h3v2a3 3 0 0 1-3 3", "M7 5H4v2a3 3 0 0 0 3 3",
    )
    val Play = GameIconSpec("M7 4.5v15a1 1 0 0 0 1.5.9l12-7.5a1 1 0 0 0 0-1.8l-12-7.5A1 1 0 0 0 7 4.5z")
    val Check = GameIconSpec("M5 12l5 5 9-10")
    val Back = GameIconSpec("M15 5l-7 7 7 7")
    val Map = GameIconSpec("M9 4L3 6v14l6-2 6 2 6-2V4l-6 2z", "M9 4v14", "M15 6v14")
    val Sliders = GameIconSpec(
        "M4 6h16", "M4 12h16", "M4 18h16",
        "M7 6a2 2 0 1 0 4 0a2 2 0 1 0-4 0z",
        "M13 12a2 2 0 1 0 4 0a2 2 0 1 0-4 0z",
        "M6 18a2 2 0 1 0 4 0a2 2 0 1 0-4 0z",
    )
    val Backspace = GameIconSpec("M21 5H9l-6 7 6 7h12a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1z", "M17 9l-5 6", "M12 9l5 6")
    val Touch = GameIconSpec(
        "M9 11V5a2 2 0 0 1 4 0v5", "M13 10V8a2 2 0 0 1 4 0v4",
        "M17 11a2 2 0 0 1 4 0v3a7 7 0 0 1-7 7h-1a7 7 0 0 1-6-3l-2.6-4.4a1.8 1.8 0 0 1 3.1-1.8L9 13",
    )
    val Sparkle = GameIconSpec("M12 2l2 7 7 3-7 3-2 7-2-7-7-3 7-3z")
    val Shield = GameIconSpec("M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z")
    val Store = GameIconSpec("M3 9l2-5h14l2 5", "M3 9h18", "M5 9v11h14V9", "M10 20v-6h4v6")
    val Lock = GameIconSpec("M6 11h12v9H6z", "M8 11V8a4 4 0 0 1 8 0v3")
}

@Composable
fun GameIcon(
    icon: GameIconSpec,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
    fill: Color? = null,
    strokeWidth: Float = 2.4f,
    contentDescription: String? = null,
) {
    val paths = remember(icon) { icon.paths.map { PathParser().parsePathString(it).toPath() } }
    val semantics = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Canvas(modifier = modifier.size(size).then(semantics)) {
        scale(scale = this.size.minDimension / 24f, pivot = Offset.Zero) {
            paths.forEach { path ->
                if (fill != null) drawPath(path, fill)
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}
