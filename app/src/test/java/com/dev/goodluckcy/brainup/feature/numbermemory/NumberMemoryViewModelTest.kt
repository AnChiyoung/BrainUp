package com.dev.goodluckcy.brainup.feature.numbermemory

import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.feature.game.GamePhase
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.testing.FakeAnalyticsLogger
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
class NumberMemoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = FakeMonotonicClock()
    private val analytics = FakeAnalyticsLogger()
    private lateinit var viewModel: NumberMemoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = NumberMemoryViewModel(NumberMemoryEngine(Random(seed = 7)), clock, analytics)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    private fun TestScope.finishMemorizing() {
        advanceTimeBy(state.memorizeDurationMs)
        runCurrent()
    }

    private fun enter(digits: List<Int>) = digits.forEach(viewModel::onDigit)

    @Test
    fun `start shows three digits then switches to answering`() = runTest(dispatcher) {
        viewModel.start()
        assertEquals(GamePhase.Memorizing, state.phase)
        assertEquals(3, state.sequence.size)

        advanceTimeBy(state.memorizeDurationMs - 1)
        runCurrent()
        assertEquals(GamePhase.Memorizing, state.phase)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(GamePhase.Answering, state.phase)
    }

    @Test
    fun `input is ignored while memorizing`() = runTest(dispatcher) {
        viewModel.start()
        viewModel.onDigit(1)
        assertEquals(emptyList<Int>(), state.input)
    }

    @Test
    fun `correct answer scores and advances to next round`() = runTest(dispatcher) {
        viewModel.start()
        finishMemorizing()
        enter(state.sequence)

        assertEquals(GamePhase.Success, state.phase)
        assertEquals(30, state.score)
        assertEquals(1, state.roundsCleared)

        advanceTimeBy(NumberMemoryViewModel.SUCCESS_FEEDBACK_MS)
        runCurrent()
        assertEquals(GamePhase.Memorizing, state.phase)
        assertEquals(2, state.round)
    }

    @Test
    fun `wrong answer finishes game with result`() = runTest(dispatcher) {
        clock.nowMs = 1_000
        viewModel.start()
        finishMemorizing()
        // 1라운드 통과
        enter(state.sequence)
        advanceTimeBy(NumberMemoryViewModel.SUCCESS_FEEDBACK_MS)
        runCurrent()
        finishMemorizing()

        clock.nowMs = 13_000
        val wrong = state.sequence.toMutableList().also { it[0] = (it[0] + 1) % 10 }
        enter(wrong)

        assertEquals(GamePhase.Finished, state.phase)
        val result = viewModel.result()
        assertEquals(GameType.NUMBER_MEMORY, result.gameType)
        assertEquals(30, result.score)
        assertEquals(1, result.roundsCleared)
        assertEquals(1, result.level)
        assertEquals(12_000L, result.durationMs)
    }

    @Test
    fun `delete removes last digit`() = runTest(dispatcher) {
        viewModel.start()
        finishMemorizing()
        viewModel.onDigit(4)
        viewModel.onDigit(2)
        viewModel.onDelete()
        assertEquals(listOf(4), state.input)
    }

    @Test
    fun `memorizing restarts from the beginning after returning from background`() = runTest(dispatcher) {
        viewModel.start()
        val sequence = state.sequence
        advanceTimeBy(2_000)
        viewModel.onStop()
        advanceUntilIdle()
        assertEquals(GamePhase.Memorizing, state.phase)

        val tokenBefore = state.memorizeToken
        viewModel.onStart()
        assertEquals(tokenBefore + 1, state.memorizeToken)
        assertEquals(sequence, state.sequence)

        advanceTimeBy(state.memorizeDurationMs - 1)
        runCurrent()
        assertEquals(GamePhase.Memorizing, state.phase)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(GamePhase.Answering, state.phase)
    }

    @Test
    fun `next round waits while app is in background`() = runTest(dispatcher) {
        viewModel.start()
        finishMemorizing()
        enter(state.sequence)
        viewModel.onStop()
        advanceUntilIdle()
        assertEquals(GamePhase.Memorizing, state.phase)
        assertEquals(2, state.round)

        viewModel.onStart()
        finishMemorizing()
        assertEquals(GamePhase.Answering, state.phase)
    }

    @Test
    fun `game start is logged when started`() = runTest(dispatcher) {
        viewModel.start()
        viewModel.start()
        assertEquals(listOf(AnalyticsEvent.gameStart(GameType.NUMBER_MEMORY)), analytics.events)
    }

    @Test
    fun `continue after reward retries same round with new digits once`() = runTest(dispatcher) {
        viewModel.start()
        finishMemorizing()
        enter(state.sequence)
        advanceTimeBy(NumberMemoryViewModel.SUCCESS_FEEDBACK_MS)
        runCurrent()
        finishMemorizing()
        enter(state.sequence.map { (it + 1) % 10 })
        assertEquals(GamePhase.Finished, state.phase)
        assertEquals(true, state.canContinue)

        viewModel.continueAfterReward()
        assertEquals(GamePhase.Memorizing, state.phase)
        assertEquals(2, state.round)
        assertEquals(3, state.sequence.size)
        assertEquals(30, state.score)
        assertEquals(emptyList<Int>(), state.input)

        finishMemorizing()
        enter(state.sequence.map { (it + 1) % 10 })
        assertEquals(GamePhase.Finished, state.phase)
        assertEquals(false, state.canContinue)
        viewModel.continueAfterReward()
        assertEquals(GamePhase.Finished, state.phase)
    }

    @Test
    fun `continue is ignored before game finishes`() = runTest(dispatcher) {
        viewModel.start()
        viewModel.continueAfterReward()
        assertEquals(false, state.continueUsed)
    }
}
