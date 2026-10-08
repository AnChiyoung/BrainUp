package com.dev.goodluckcy.brainup.core.navigation

import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.GameType
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute

@Serializable
data class GameRoute(val gameType: GameType)

@Serializable
data class ResultRoute(
    val gameType: GameType,
    val score: Int,
    val level: Int,
    val roundsCleared: Int,
    val durationMs: Long,
    val medianReactionMs: Long? = null,
    val bestReactionMs: Long? = null,
) {
    fun toGameResult() = GameResult(
        gameType = gameType,
        score = score,
        level = level,
        roundsCleared = roundsCleared,
        durationMs = durationMs,
        medianReactionMs = medianReactionMs,
        bestReactionMs = bestReactionMs,
    )

    companion object {
        fun from(result: GameResult) = ResultRoute(
            gameType = result.gameType,
            score = result.score,
            level = result.level,
            roundsCleared = result.roundsCleared,
            durationMs = result.durationMs,
            medianReactionMs = result.medianReactionMs,
            bestReactionMs = result.bestReactionMs,
        )
    }
}
