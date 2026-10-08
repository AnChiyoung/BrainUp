package com.dev.goodluckcy.brainup.feature.reaction

import javax.inject.Inject
import kotlin.random.Random

/**
 * 반응 속도 게임 규칙. (플랫폼 공통 명세)
 *
 * - 대기 화면에서 [MIN_DELAY_MS]~[MAX_DELAY_MS] 사이 무작위 시간 후 색이 바뀐다.
 * - 색이 바뀌기 전 터치는 무효(기록하지 않음)이며 같은 시도를 다시 진행한다.
 * - 유효한 [ATTEMPTS]회의 중앙값과 최단 시간을 결과로 한다.
 * - 점수는 (1000 - 중앙값ms)이며 0~1000으로 제한한다.
 */
class ReactionEngine @Inject constructor(
    private val random: Random,
) {
    fun randomDelayMs(): Long = random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1)

    fun median(reactionsMs: List<Long>): Long {
        require(reactionsMs.isNotEmpty()) { "reactions must not be empty" }
        val sorted = reactionsMs.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
    }

    fun best(reactionsMs: List<Long>): Long {
        require(reactionsMs.isNotEmpty()) { "reactions must not be empty" }
        return reactionsMs.min()
    }

    fun score(medianMs: Long): Int = (MAX_SCORE - medianMs).coerceIn(0L, MAX_SCORE).toInt()

    companion object {
        const val ATTEMPTS = 5
        const val MIN_DELAY_MS = 2_000L
        const val MAX_DELAY_MS = 5_000L
        const val MAX_SCORE = 1_000L
    }
}
