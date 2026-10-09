package com.dev.goodluckcy.brainup.feature.numbermemory

import javax.inject.Inject
import kotlin.random.Random

/**
 * 숫자 기억력 게임 규칙. (플랫폼 공통 명세)
 *
 * - 라운드는 1부터 시작하며 [ROUNDS_PER_LEVEL] 라운드마다 레벨이 1 오른다.
 * - 레벨 1은 숫자 [START_LENGTH]개, 레벨이 오를 때마다 1개씩 늘어난다(최대 [MAX_LENGTH]).
 * - 표시 시간은 3개 기준 1초, 숫자 1개당 0.2초씩 늘어난다.
 * - 라운드 성공 시 (숫자 개수 × [POINTS_PER_DIGIT])점을 얻는다.
 */
class NumberMemoryEngine @Inject constructor(
    private val random: Random,
) {
    fun levelForRound(round: Int): Int {
        require(round >= 1) { "round must be >= 1: $round" }
        return 1 + (round - 1) / ROUNDS_PER_LEVEL
    }

    fun lengthForRound(round: Int): Int =
        (START_LENGTH + levelForRound(round) - 1).coerceAtMost(MAX_LENGTH)

    fun memorizeDurationMs(length: Int): Long =
        BASE_MEMORIZE_MS + (length - START_LENGTH).coerceAtLeast(0) * MEMORIZE_MS_PER_EXTRA_DIGIT

    fun generateSequence(length: Int): List<Int> = List(length) { random.nextInt(0, 10) }

    fun isCorrect(expected: List<Int>, input: List<Int>): Boolean = expected == input

    fun scoreForRound(length: Int): Int = length * POINTS_PER_DIGIT

    companion object {
        const val START_LENGTH = 3
        const val MAX_LENGTH = 15
        const val ROUNDS_PER_LEVEL = 2
        const val POINTS_PER_DIGIT = 10
        const val BASE_MEMORIZE_MS = 1_000L
        const val MEMORIZE_MS_PER_EXTRA_DIGIT = 200L
    }
}
