package com.dev.goodluckcy.brainup.feature.pattern

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PatternMemoryEngineTest {

    private val engine = PatternMemoryEngine(Random(seed = 11))

    @Test
    fun `initial sequence has start length and valid tiles`() {
        repeat(100) {
            val sequence = engine.initialSequence()
            assertEquals(PatternMemoryEngine.START_LENGTH, sequence.size)
            assertTrue(sequence.all { it in 0 until PatternMemoryEngine.TILE_COUNT })
        }
    }

    @Test
    fun `extend keeps existing order and adds one tile`() {
        val sequence = listOf(0, 4, 8)
        val extended = engine.extend(sequence)
        assertEquals(4, extended.size)
        assertEquals(sequence, extended.take(3))
    }

    @Test
    fun `same tile never appears twice in a row`() {
        var sequence = engine.initialSequence()
        repeat(500) { sequence = engine.extend(sequence) }
        sequence.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
    }

    @Test
    fun `every tile can be chosen`() {
        var sequence = engine.initialSequence()
        repeat(500) { sequence = engine.extend(sequence) }
        assertEquals((0 until PatternMemoryEngine.TILE_COUNT).toSet(), sequence.toSet())
    }

    @Test
    fun `tap is correct only for tile at current index`() {
        val sequence = listOf(2, 5, 7)
        assertTrue(engine.isCorrectTap(sequence, 1, 5))
        assertFalse(engine.isCorrectTap(sequence, 1, 7))
        assertFalse(engine.isCorrectTap(sequence, 3, 2))
    }

    @Test
    fun `lit duration shortens with length down to minimum`() {
        assertEquals(500L, engine.litDurationMs(3))
        assertEquals(400L, engine.litDurationMs(8))
        assertEquals(PatternMemoryEngine.MIN_LIT_MS, engine.litDurationMs(50))
    }

    @Test
    fun `score is ten points per tile`() {
        assertEquals(30, engine.scoreForRound(3))
        assertEquals(60, engine.scoreForRound(6))
    }
}
