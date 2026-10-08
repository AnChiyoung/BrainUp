package com.dev.goodluckcy.brainup.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIconSpec
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.theme.Mint
import com.dev.goodluckcy.brainup.core.designsystem.theme.MintDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.OrangeDark
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.SkyDark
import com.dev.goodluckcy.brainup.domain.model.GameType

@get:StringRes
val GameType.titleRes: Int
    get() = when (this) {
        GameType.NUMBER_MEMORY -> R.string.game_number_memory
        GameType.REACTION -> R.string.game_reaction
        GameType.PATTERN -> R.string.game_pattern
    }

@get:StringRes
val GameType.descriptionRes: Int
    get() = when (this) {
        GameType.NUMBER_MEMORY -> R.string.game_number_memory_desc
        GameType.REACTION -> R.string.game_reaction_desc
        GameType.PATTERN -> R.string.game_pattern_desc
    }

/** 게임별 고유 색 */
val GameType.color: Color
    get() = when (this) {
        GameType.NUMBER_MEMORY -> Orange
        GameType.REACTION -> Mint
        GameType.PATTERN -> Sky
    }

/** 게임 색 버튼의 그림자·리본 꼬리 색 */
val GameType.darkColor: Color
    get() = when (this) {
        GameType.NUMBER_MEMORY -> OrangeDark
        GameType.REACTION -> MintDark
        GameType.PATTERN -> SkyDark
    }

val GameType.icon: GameIconSpec
    get() = when (this) {
        GameType.NUMBER_MEMORY -> GameIcons.Hash
        GameType.REACTION -> GameIcons.Bolt
        GameType.PATTERN -> GameIcons.Grid
    }
