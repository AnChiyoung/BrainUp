package com.dev.goodluckcy.brainup.domain.model

import java.time.LocalDate

object Streak {
    /**
     * 연속 완료 일수.
     * 오늘을 아직 완료하지 않았어도 어제까지 이어진 기록은 유지된다(오늘 안에 완료하면 이어짐).
     * 불꽃 방패로 지킨 날([shieldedDates])은 기록을 끊지 않지만 일수에는 더하지 않는다.
     *
     * @param completedDates 오늘의 도전을 완료한 날짜(순서·중복 무관)
     */
    fun current(
        completedDates: Collection<LocalDate>,
        today: LocalDate,
        shieldedDates: Collection<LocalDate> = emptyList(),
    ): Int {
        val completed = completedDates.toSet()
        val start = if (today in completed) today else today.minusDays(1)
        return countFrom(start, completed, shieldedDates.toSet())
    }

    /** [day]부터 거꾸로 이어진 연속 일수. [day]가 끊긴 날이면 0. */
    fun countFrom(day: LocalDate, completed: Set<LocalDate>, shielded: Set<LocalDate>): Int {
        var current = day
        var count = 0
        while (current in completed || current in shielded) {
            if (current in completed) count++
            current = current.minusDays(1)
        }
        return count
    }
}
