package com.dev.goodluckcy.brainup.feature.colorrun

import com.dev.goodluckcy.brainup.feature.colorrun.ColorRunEngine.Companion.GATE_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ColorRunEngineTest {

    private val engine = ColorRunEngine(Random(42))

    private fun correct(state: ColorRunState) = engine.tap(state, state.nextGateColor!!)
    private fun wrong(state: ColorRunState) = engine.tap(state, (state.nextGateColor!! + 1) % ColorRunEngine.COLORS)

    @Test
    fun `gates use fewer colors first and never repeat back to back`() {
        repeat(20) {
            val gates = engine.newRun().gateColors
            assertEquals(GATE_COUNT, gates.size)
            gates.zipWithNext { a, b -> assertNotEquals(a, b) }
            gates.forEachIndexed { index, color -> assertTrue(color < ColorRunEngine.colorCountFor(index)) }
        }
    }

    @Test
    fun `correct color opens gate and builds combo`() {
        val state = correct(correct(engine.newRun()))
        assertEquals(2, state.openedGates)
        assertEquals(2, state.combo)
        assertEquals(2, state.cleanGates)
    }

    @Test
    fun `wrong color stuns and resets combo`() {
        val state = wrong(correct(engine.newRun()))
        assertEquals(1, state.openedGates)
        assertEquals(0, state.combo)
        assertEquals(ColorRunEngine.STUN_MS, state.stunRemainingMs)
        // 넘어진 동안에는 누를 수 없고 움직이지도 않는다.
        assertEquals(state, correct(state))
        val stunned = engine.tick(state, 300)
        assertEquals(state.distance, stunned.distance)
        assertEquals(ColorRunEngine.STUN_MS - 300, stunned.stunRemainingMs)
    }

    @Test
    fun `gate opened after a mistake is not clean`() {
        var state = wrong(engine.newRun())
        state = engine.tick(state, ColorRunEngine.STUN_MS)
        state = correct(state)
        assertEquals(1, state.openedGates)
        assertEquals(0, state.cleanGates)
    }

    @Test
    fun `closed gate blocks the runner`() {
        val state = engine.tick(engine.newRun(), 10_000)
        assertEquals(ColorRunEngine.gatePosition(0), state.distance)
        assertTrue(state.blocked)
        val opened = correct(state)
        assertFalse(opened.blocked)
        assertEquals(0, opened.cleanGates)
        assertTrue(engine.tick(opened, 100).distance > state.distance)
    }

    @Test
    fun `combo makes the runner faster`() {
        val slow = engine.tick(engine.newRun(), 1_000)
        val fast = engine.tick(correct(correct(engine.newRun())), 1_000)
        assertTrue(fast.distance > slow.distance)
    }

    @Test
    fun `opening every gate reaches the finish`() {
        var state = engine.newRun()
        repeat(GATE_COUNT) { state = correct(state) }
        while (!state.isFinished) state = engine.tick(state, 100)
        assertEquals(ColorRunEngine.FINISH_DISTANCE, state.distance)
        assertEquals(GATE_COUNT, state.cleanGates)
        assertEquals(ColorRunEngine.MAX_COMBO, state.maxCombo)
    }

    @Test
    fun `faster finish scores higher`() {
        assertEquals(200, engine.score(20_000))
        assertTrue(engine.score(25_000) > engine.score(40_000))
    }

    @Test
    fun `buttons cover the colors in play`() {
        val state = engine.newRun()
        assertEquals((0 until ColorRunEngine.EASY_COLORS).toSet(), state.buttonOrder.toSet())
        var later = state
        repeat(ColorRunEngine.EASY_GATES) { later = correct(later) }
        // 셔플 시점이 지나면 4색 버튼
        repeat(ColorRunEngine.SHUFFLE_EVERY) { later = correct(later) }
        assertEquals((0 until ColorRunEngine.COLORS).toSet(), later.buttonOrder.toSet())
    }

    @Test
    fun `next gate color always has a button`() {
        repeat(20) {
            var state = engine.newRun()
            while (state.nextGateColor != null) {
                assertTrue(state.nextGateColor in state.buttonOrder)
                state = correct(state)
            }
        }
    }
}
