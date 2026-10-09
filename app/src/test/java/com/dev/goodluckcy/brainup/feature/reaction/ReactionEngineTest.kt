package com.dev.goodluckcy.brainup.feature.reaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReactionEngineTest {

    private val engine = ReactionEngine(Random(seed = 3))

    @Test
    fun `random delay is between two and five seconds`() {
        repeat(1_000) {
            assertTrue(engine.randomDelayMs() in 2_000L..5_000L)
        }
    }

    @Test
    fun `median of odd count is middle value`() {
        assertEquals(300L, engine.median(listOf(400, 250, 300, 280, 350)))
    }

    @Test
    fun `median of even count is average of middle values`() {
        assertEquals(290L, engine.median(listOf(300, 250, 280, 400)))
    }

    @Test
    fun `best is shortest reaction`() {
        assertEquals(250L, engine.best(listOf(400, 250, 300)))
    }

    @Test
    fun `score is thousand minus median clamped to range`() {
        assertEquals(700, engine.score(300))
        assertEquals(0, engine.score(1_500))
        assertEquals(1_000, engine.score(0))
    }
}
