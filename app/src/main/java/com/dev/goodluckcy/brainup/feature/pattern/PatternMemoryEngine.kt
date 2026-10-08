package com.dev.goodluckcy.brainup.feature.pattern

import javax.inject.Inject
import kotlin.random.Random

/**
 * 패턴 기억 게임 규칙. (플랫폼 공통 명세)
 *
 * - 3×3 타일(0~8)을 순서대로 점등하고, 사용자는 같은 순서로 터치한다.
 * - 1라운드는 [START_LENGTH]개이며, 라운드마다 기존 순서 뒤에 1개씩 추가된다.
 * - 같은 타일이 연속으로 나오지 않는다.
 * - 점등 시간은 [BASE_LIT_MS]에서 길이가 1 늘 때마다 [LIT_MS_STEP]씩 줄며 [MIN_LIT_MS] 이상이다.
 * - 라운드 성공 시 (패턴 길이 × [POINTS_PER_TILE])점을 얻는다.
 */
class PatternMemoryEngine @Inject constructor(
    private val random: Random,
) {
    fun initialSequence(): List<Int> =
        (1..START_LENGTH).fold(emptyList()) { sequence, _ -> extend(sequence) }

    fun extend(sequence: List<Int>): List<Int> = sequence + nextTile(previous = sequence.lastOrNull())

    fun isCorrectTap(sequence: List<Int>, index: Int, tile: Int): Boolean = sequence.getOrNull(index) == tile

    fun litDurationMs(length: Int): Long =
        (BASE_LIT_MS - (length - START_LENGTH).coerceAtLeast(0) * LIT_MS_STEP).coerceAtLeast(MIN_LIT_MS)

    fun scoreForRound(length: Int): Int = length * POINTS_PER_TILE

    private fun nextTile(previous: Int?): Int {
        if (previous == null) return random.nextInt(TILE_COUNT)
        // previous를 제외한 8개 중에서 고른다.
        val candidate = random.nextInt(TILE_COUNT - 1)
        return if (candidate >= previous) candidate + 1 else candidate
    }

    companion object {
        const val GRID_SIZE = 3
        const val TILE_COUNT = GRID_SIZE * GRID_SIZE
        const val START_LENGTH = 3
        const val POINTS_PER_TILE = 10
        const val BASE_LIT_MS = 500L
        const val LIT_MS_STEP = 20L
        const val MIN_LIT_MS = 300L
        const val GAP_MS = 250L
    }
}
