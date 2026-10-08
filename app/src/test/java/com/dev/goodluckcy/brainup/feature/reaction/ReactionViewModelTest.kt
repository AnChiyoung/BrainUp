package com.dev.goodluckcy.brainup.feature.reaction

import com.dev.goodluckcy.brainup.domain.model.GameType
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
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = FakeMonotonicClock()
    private lateinit var viewModel: ReactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = ReactionViewModel(ReactionEngine(Random(seed = 5)), clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    /** 대기 시작 → 색 변경까지 진행한다. */
    private fun TestScope.waitUntilGo() {
        viewModel.onTap()
        advanceUntilIdle()
        assertEquals(ReactionPhase.Go, state.phase)
    }

    private fun TestScope.react(reactionMs: Long) {
        waitUntilGo()
        clock.advanceMs(reactionMs)
        viewModel.onTap()
    }

    @Test
    fun `color does not change before minimum delay`() = runTest(dispatcher) {
        viewModel.onTap()
        assertEquals(ReactionPhase.Waiting, state.phase)
        advanceTimeBy(ReactionEngine.MIN_DELAY_MS - 1)
        runCurrent()
        assertEquals(ReactionPhase.Waiting, state.phase)
    }

    @Test
    fun `reaction time is measured from color change`() = runTest(dispatcher) {
        react(250)
        assertEquals(ReactionPhase.AttemptResult, state.phase)
        assertEquals(listOf(250L), state.reactionsMs)
    }

    @Test
    fun `tap before color change is a false start and not recorded`() = runTest(dispatcher) {
        viewModel.onTap()
        advanceTimeBy(1_000)
        viewModel.onTap()

        assertEquals(ReactionPhase.TooEarly, state.phase)
        assertEquals(1, state.falseStarts)
        assertEquals(emptyList<Long>(), state.reactionsMs)

        // 취소된 타이머가 뒤늦게 Go로 바꾸지 않아야 한다.
        advanceUntilIdle()
        assertEquals(ReactionPhase.TooEarly, state.phase)
    }

    @Test
    fun `finishes after five valid attempts with median best and score`() = runTest(dispatcher) {
        listOf(300L, 250L, 400L, 280L, 350L).forEach { react(it) }

        assertEquals(ReactionPhase.Finished, state.phase)
        val result = viewModel.result()
        assertEquals(GameType.REACTION, result.gameType)
        assertEquals(300L, result.medianReactionMs)
        assertEquals(250L, result.bestReactionMs)
        assertEquals(700, result.score)
        assertEquals(5, result.roundsCleared)
    }

    @Test
    fun `taps are ignored after finishing`() = runTest(dispatcher) {
        repeat(ReactionEngine.ATTEMPTS) { react(300) }
        viewModel.onTap()
        advanceUntilIdle()
        assertEquals(ReactionPhase.Finished, state.phase)
    }

    @Test
    fun `going to background while waiting cancels attempt but keeps records`() = runTest(dispatcher) {
        react(300)
        viewModel.onTap()
        viewModel.onStop()
        advanceUntilIdle()

        assertEquals(ReactionPhase.Ready, state.phase)
        assertEquals(listOf(300L), state.reactionsMs)
    }
}
