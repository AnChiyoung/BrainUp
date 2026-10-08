package com.dev.goodluckcy.brainup.domain.model

import androidx.annotation.Keep

/**
 * 미니게임 종류. [bit]은 DailyProgress.completedMask에서 사용하는 비트 값이다.
 * [analyticsName]은 Android/iOS 공통 Analytics `game_type` 파라미터 값이다.
 *
 * 내비게이션 경로 인자로 쓰이므로 R8이 이름을 바꾸지 않도록 [Keep]한다.
 */
@Keep
enum class GameType(val bit: Int, val analyticsName: String) {
    NUMBER_MEMORY(1 shl 0, "number_memory"),
    REACTION(1 shl 1, "reaction"),
    PATTERN(1 shl 2, "pattern");

    companion object {
        const val ALL_COMPLETED_MASK = 0b111
    }
}
