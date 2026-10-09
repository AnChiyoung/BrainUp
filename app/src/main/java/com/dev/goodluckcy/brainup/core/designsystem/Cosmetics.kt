package com.dev.goodluckcy.brainup.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconSpec
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.theme.Brainy
import com.dev.goodluckcy.brainup.core.designsystem.theme.Go
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Pink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyGlow
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark
import com.dev.goodluckcy.brainup.domain.model.ShopItem

/** 상점 아이템 이름 */
@get:StringRes
val ShopItem.nameRes: Int
    get() = when (this) {
        ShopItem.SHIELD -> R.string.item_shield
        ShopItem.BRAINY_PINK -> R.string.item_brainy_pink
        ShopItem.BRAINY_MINT -> R.string.item_brainy_mint
        ShopItem.BRAINY_SKY -> R.string.item_brainy_sky
        ShopItem.BRAINY_YELLOW -> R.string.item_brainy_yellow
        ShopItem.BRAINY_RAINBOW -> R.string.item_brainy_rainbow
        ShopItem.TILE_GEM -> R.string.item_tile_gem
        ShopItem.TILE_FRUIT -> R.string.item_tile_fruit
        ShopItem.TILE_STAR -> R.string.item_tile_star
        ShopItem.THEME_NIGHT -> R.string.item_theme_night
        ShopItem.THEME_OCEAN -> R.string.item_theme_ocean
        ShopItem.THEME_SPACE -> R.string.item_theme_space
    }

private val RainbowBrush = Brush.verticalGradient(listOf(Orange, Sun, Go, SkyGlow))

/** 마스코트 브레니 바탕(색 아이템) */
fun Modifier.brainyBackground(item: ShopItem, shape: Shape): Modifier = when (item) {
    ShopItem.BRAINY_RAINBOW -> background(RainbowBrush, shape)
    ShopItem.BRAINY_MINT -> background(Go, shape)
    ShopItem.BRAINY_SKY -> background(SkyGlow, shape)
    ShopItem.BRAINY_YELLOW -> background(Sun, shape)
    else -> background(Brainy, shape)
}

/** 지도 배경 테마 */
data class SkyPalette(val ground: Color, val planet: Color, val accentPlanet: Color? = null)

val ShopItem.skyPalette: SkyPalette
    get() = when (this) {
        ShopItem.THEME_OCEAN -> SkyPalette(ground = Color(0xFF0B7285), planet = Color(0xFF1098AD))
        ShopItem.THEME_SPACE -> SkyPalette(ground = Color(0xFF14142B), planet = Color(0xFF26264A), accentPlanet = Orange)
        else -> SkyPalette(ground = Night, planet = NightLight)
    }

/** 코인 수를 1,240처럼 천 단위로 끊어 쓴다. */
fun formatCoins(amount: Int): String = "%,d".format(java.util.Locale.US, amount)

/** 패턴 기억 타일 스킨: 모양과 불이 켜졌을 때의 색 */
data class TileSkinStyle(
    val shape: Shape,
    /** 동그란 타일은 빛 번짐도 동그랗게, 아래 띠 없이 그린다. */
    val round: Boolean,
    val lit: Color,
    val litBottom: Color,
    val litIcon: GameIconSpec,
    val litIconTint: Color,
    val litIconFill: Color,
)

val ShopItem.tileSkin: TileSkinStyle
    get() = when (this) {
        ShopItem.TILE_FRUIT -> TileSkinStyle(
            shape = CircleShape,
            round = true,
            lit = Pink,
            litBottom = Color(0xFFD63A68),
            litIcon = GameIcons.Heart,
            litIconTint = Color.White,
            litIconFill = Color.White,
        )
        ShopItem.TILE_STAR -> TileSkinStyle(
            shape = RoundedCornerShape(22.dp),
            round = false,
            lit = Sun,
            litBottom = SunDark,
            litIcon = GameIcons.Star,
            litIconTint = Ink,
            litIconFill = Color.White,
        )
        else -> TileSkinStyle(
            shape = RoundedCornerShape(22.dp),
            round = false,
            lit = SkyGlow,
            litBottom = Sky,
            litIcon = GameIcons.Sparkle,
            litIconTint = Color.White,
            litIconFill = Color.White,
        )
    }
