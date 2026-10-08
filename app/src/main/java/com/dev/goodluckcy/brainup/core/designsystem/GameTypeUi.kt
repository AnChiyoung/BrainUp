package com.dev.goodluckcy.brainup.core.designsystem

import androidx.annotation.StringRes
import com.dev.goodluckcy.brainup.R
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
