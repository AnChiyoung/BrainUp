package com.dev.goodluckcy.brainup.feature.pattern

import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.feature.game.GamePhase
import com.dev.goodluckcy.brainup.testing.FakeMonotonicClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class PatternMemoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = FakeMonotonicClock()
    private lateinit var viewModel: PatternMemoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = PatternMemoryViewModel(PatternMemoryEngine(Random(seed = 13)), clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    private fun TestScope.finishPlayback() {
        advanceUntilIdle()
        assertEquals(GamePhase.Answering, state.phase)
    }

    private fun wrongTileFor(index: Int): Int = (state.sequence[index] + 1) % PatternMemoryEngine.TILE_COUNT

    @Test
    fun `playback lights each tile in order then waits for input`() = runTest(dispatcher) {
        viewModel.start()
        val sequence = state.sequence
        val litMs = PatternMemoryEngine.BASE_LIT_MS
        val stepMs = litMs + PatternMemoryEngine.GAP_MS

        advanceTimeBy(PatternMemoryViewModel.PLAYBACK_START_DELAY_MS + 1)
        runCurrent()
        assertEquals(sequence[0], state.litTile)

        advanceTimeBy(litMs)
        runCurrent()
        assertNull(state.litTile)

        advanceTimeBy(stepMs - litMs)
        runCurrent()
        assertEquals(sequence[1], state.litTile)

        finishPlayback()
        assertNull(state.litTile)
    }

    @Test
    fun `taps are ignored during playback`() = runTest(dispatcher) {
        viewModel.start()
        viewModel.onTileTap(state.sequence[0])
        assertEquals(0, state.inputCount)
        assertEquals(GamePhase.Memorizing, state.phase)
    }

    @Test
    fun `correct taps clear round and extend same pattern by one`() = runTest(dispatcher) {
        viewModel.start()
        finishPlayback()
        val sequence = state.sequence
        sequence.forEach(viewModel::onTileTap)

        assertEquals(GamePhase.Success, state.phase)
        assertEquals(30, state.score)
        assertEquals(1, state.roundsCleared)

        advanceTimeBy(PatternMemoryViewModel.SUCCESS_FEEDBACK_MS)
        runCurrent()
        assertEquals(GamePhase.Memorizing, state.phase)
        assertEquals(2, state.round)
        assertEquals(sequence, state.sequence.take(sequence.size))
        assertEquals(sequence.size + 1, state.sequence.size)
    }

    @Test
    fun `wrong tap finishes game and marks wrong and expected tiles`() = runTest(dispatcher) {
        clock.nowMs = 500
        viewModel.start()
        finishPlayback()
        state.sequence.forEach(viewModel::onTileTap)
        advanceTimeBy(PatternMemoryViewModel.SUCCESS_FEEDBACK_MS)
        finishPlayback()

        viewModel.onTileTap(state.sequence[0])
        clock.nowMs = 20_500
        val wrong = wrongTileFor(1)
        viewModel.onTileTap(wrong)

        assertEquals(GamePhase.Finished, state.phase)
        assertEquals(wrong, state.wrongTile)
        assertEquals(state.sequence[1], state.expectedTile)

        val result = viewModel.result()
        assertEquals(GameType.PATTERN, result.gameType)
        assertEquals(30, result.score)
        assertEquals(1, result.roundsCleared)
        assertEquals(3, result.level)
        assertEquals(20_000L, result.durationMs)
    }

    @Test
    fun `failing first round reports zero length remembered`() = runTest(dispatcher) {
        viewModel.start()
        finishPlayback()
        viewModel.onTileTap(wrongTileFor(0))
        assertEquals(0, viewModel.result().level)
    }

    @Test
    fun `tapped tile flashes briefly`() = runTest(dispatcher) {
        viewModel.start()
        finishPlayback()
        viewModel.onTileTap(state.sequence[0])
        assertEquals(state.sequence[0], state.flashedTile)
        advanceTimeBy(PatternMemoryViewModel.TAP_FLASH_MS)
        runCurrent()
        assertNull(state.flashedTile)
    }

    @Test
    fun `playback restarts from beginning after returning from background`() = runTest(dispatcher) {
        viewModel.start()
        val sequence = state.sequence
        advanceTimeBy(PatternMemoryViewModel.PLAYBACK_START_DELAY_MS + 1)
        runCurrent()

        viewModel.onStop()
        assertNull(state.litTile)
        advanceUntilIdle()
        assertEquals(GamePhase.Memorizing, state.phase)

        viewModel.onStart()
        assertEquals(sequence, state.sequence)
        advanceTimeBy(PatternMemoryViewModel.PLAYBACK_START_DELAY_MS + 1)
        runCurrent()
        assertEquals(sequence[0], state.litTile)
        finishPlayback()
    }
}
