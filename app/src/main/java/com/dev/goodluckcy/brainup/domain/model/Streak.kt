package com.dev.goodluckcy.brainup.domain.model

import java.time.LocalDate

object Streak {
    /**
     * 연속 완료 일수.
     * 오늘을 아직 완료하지 않았어도 어제까지 이어진 기록은 유지된다(오늘 안에 완료하면 이어짐).
     *
     * @param completedDates 완료한 날짜(순서·중복 무관)
     */
    fun current(completedDates: Collection<LocalDate>, today: LocalDate): Int {
        val dates = completedDates.toSet()
        var day = if (today in dates) today else today.minusDays(1)
        var count = 0
        while (day in dates) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
