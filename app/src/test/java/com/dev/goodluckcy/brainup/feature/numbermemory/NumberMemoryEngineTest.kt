package com.dev.goodluckcy.brainup.feature.numbermemory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NumberMemoryEngineTest {

    private val engine = NumberMemoryEngine(Random(seed = 1))

    @Test
    fun `level increases every two rounds`() {
        assertEquals(listOf(1, 1, 2, 2, 3), (1..5).map(engine::levelForRound))
    }

    @Test
    fun `length starts at three and grows with level`() {
        assertEquals(listOf(3, 3, 4, 4, 5), (1..5).map(engine::lengthForRound))
    }

    @Test
    fun `length is capped at max length`() {
        assertEquals(NumberMemoryEngine.MAX_LENGTH, engine.lengthForRound(1_000))
    }

    @Test
    fun `memorize duration is three seconds for three digits plus half second per extra digit`() {
        assertEquals(3_000L, engine.memorizeDurationMs(3))
        assertEquals(4_000L, engine.memorizeDurationMs(5))
    }

    @Test
    fun `generated sequence has requested length and only single digits`() {
        repeat(100) {
            val sequence = engine.generateSequence(8)
            assertEquals(8, sequence.size)
            assertTrue(sequence.all { it in 0..9 })
        }
    }

    @Test
    fun `answer must match in order`() {
        assertTrue(engine.isCorrect(listOf(1, 2, 3), listOf(1, 2, 3)))
        assertFalse(engine.isCorrect(listOf(1, 2, 3), listOf(1, 3, 2)))
    }

    @Test
    fun `score is ten points per digit`() {
        assertEquals(30, engine.scoreForRound(3))
        assertEquals(70, engine.scoreForRound(7))
    }
}
