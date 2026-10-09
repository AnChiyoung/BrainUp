package com.dev.goodluckcy.brainup.feature.colorrun

import javax.inject.Inject
import kotlin.random.Random

/**
 * 색깔 달리기 규칙. (플랫폼 공통 명세)
 *
 * - 브레니가 결승선을 향해 자동으로 달리고, 길 위에 색 문이 [GATE_COUNT]개 있다.
 * - 바로 앞 문과 같은 색 버튼을 누르면 문이 열리고 콤보가 오른다. 콤보만큼 빨라진다.
 * - 다른 색을 누르면 [STUN_MS] 동안 넘어지고 콤보가 사라진다.
 * - 닫힌 문에 닿으면 열 때까지 멈추고 콤보가 사라진다.
 * - 처음 [EASY_GATES]개 문은 [EASY_COLORS]색, 그 뒤는 [COLORS]색. 같은 색 문은 연달아 나오지 않는다.
 * - 버튼 순서는 [SHUFFLE_EVERY]개 문마다 섞인다.
 * - 점수는 결승선까지 걸린 시간이 짧을수록 높다.
 */
class ColorRunEngine @Inject constructor(
    private val random: Random,
) {
    fun newRun(): ColorRunState {
        val gates = (0 until GATE_COUNT).fold(emptyList<Int>()) { acc, index ->
            val colors = colorCountFor(index)
            var color: Int
            do {
                color = random.nextInt(colors)
            } while (color == acc.lastOrNull())
            acc + color
        }
        return ColorRunState(gateColors = gates, buttonOrder = shuffledButtons(0))
    }

    /** [elapsedMs]만큼 시간을 흘린다. */
    fun tick(state: ColorRunState, elapsedMs: Long): ColorRunState {
        if (state.isFinished || elapsedMs <= 0) return state
        var next = state.copy(elapsedMs = state.elapsedMs + elapsedMs)
        if (next.stunRemainingMs > 0) {
            return next.copy(stunRemainingMs = (next.stunRemainingMs - elapsedMs).coerceAtLeast(0))
        }
        val moved = next.distance + speedFor(next.combo) * elapsedMs / 1000f
        val gate = next.nextGatePosition
        next = if (gate != null && moved >= gate) {
            // 닫힌 문 앞에서 멈춘다. 처음 부딪힐 때만 콤보가 사라진다.
            next.copy(distance = gate, combo = 0, blocked = true)
        } else {
            next.copy(distance = moved.coerceAtMost(FINISH_DISTANCE))
        }
        return if (next.distance >= FINISH_DISTANCE) next.copy(isFinished = true) else next
    }

    /** 색 버튼을 누른다. */
    fun tap(state: ColorRunState, color: Int): ColorRunState {
        if (state.isFinished || state.stunRemainingMs > 0) return state
        val expected = state.nextGateColor ?: return state
        if (color != expected) {
            return state.copy(stunRemainingMs = STUN_MS, combo = 0, mistakes = state.mistakes + 1, gateHadMistake = true)
        }
        val clean = !state.blocked && !state.gateHadMistake
        val combo = (state.combo + 1).coerceAtMost(MAX_COMBO)
        val opened = state.openedGates + 1
        return state.copy(
            openedGates = opened,
            combo = combo,
            maxCombo = maxOf(state.maxCombo, combo),
            cleanGates = state.cleanGates + if (clean) 1 else 0,
            blocked = false,
            gateHadMistake = false,
            // 색 수가 늘어나는 문에서도 버튼을 새로 만든다.
            buttonOrder = if (opened % SHUFFLE_EVERY == 0 || colorCountFor(opened) != state.buttonOrder.size) {
                shuffledButtons(opened)
            } else {
                state.buttonOrder
            },
        )
    }

    fun score(elapsedMs: Long): Int = (SCORE_FACTOR / elapsedMs.coerceAtLeast(1)).toInt()

    private fun shuffledButtons(openedGates: Int): List<Int> =
        (0 until colorCountFor(openedGates.coerceAtMost(GATE_COUNT - 1))).shuffled(random)

    companion object {
        const val GATE_COUNT = 20
        const val GATE_SPACING = 10f
        const val FINISH_DISTANCE = GATE_SPACING * (GATE_COUNT + 1)
        const val BASE_SPEED = 8f
        const val SPEED_PER_COMBO = 1.2f
        const val MAX_COMBO = 5
        const val STUN_MS = 700L
        const val EASY_GATES = 6
        const val EASY_COLORS = 3
        const val COLORS = 4
        const val SHUFFLE_EVERY = 5

        /** 20초에 결승선을 통과하면 200점 */
        const val SCORE_FACTOR = 4_000_000L

        fun colorCountFor(gateIndex: Int): Int = if (gateIndex < EASY_GATES) EASY_COLORS else COLORS

        /** 초당 이동 거리 */
        fun speedFor(combo: Int): Float = BASE_SPEED + combo * SPEED_PER_COMBO

        fun gatePosition(index: Int): Float = GATE_SPACING * (index + 1)
    }
}

data class ColorRunState(
    /** 문 색(0 ~ [ColorRunEngine.COLORS] - 1), 가까운 문부터 */
    val gateColors: List<Int>,
    /** 화면 아래 버튼의 색 순서 */
    val buttonOrder: List<Int>,
    val distance: Float = 0f,
    val openedGates: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    /** 실수하거나 멈추지 않고 연 문 수 */
    val cleanGates: Int = 0,
    val mistakes: Int = 0,
    val stunRemainingMs: Long = 0,
    /** 닫힌 문 앞에 멈춰 있음 */
    val blocked: Boolean = false,
    val gateHadMistake: Boolean = false,
    val elapsedMs: Long = 0,
    val isFinished: Boolean = false,
) {
    val nextGateColor: Int? get() = gateColors.getOrNull(openedGates)
    val nextGatePosition: Float?
        get() = if (openedGates < gateColors.size) ColorRunEngine.gatePosition(openedGates) else null
    val progress: Float get() = distance / ColorRunEngine.FINISH_DISTANCE
}
