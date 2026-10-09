package com.dev.goodluckcy.brainup.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/** 기기 시간대 기준 로컬 날짜 계산. 일일 챌린지의 날짜 경계를 정한다. */
class LocalDateProvider @Inject constructor(
    private val clock: Clock,
) {
    fun nowMs(): Long = clock.millis()

    fun today(): LocalDate = LocalDate.now(clock)

    fun dateOf(epochMs: Long): LocalDate = Instant.ofEpochMilli(epochMs).atZone(clock.zone).toLocalDate()

    fun startOfDayMs(date: LocalDate): Long = date.atStartOfDay(clock.zone).toInstant().toEpochMilli()

    /** 오늘 날짜를 내보내고, 자정이 지나면 새 날짜를 내보낸다. */
    fun observeToday(): Flow<LocalDate> = flow {
        while (true) {
            val today = today()
            emit(today)
            val untilMidnight = startOfDayMs(today.plusDays(1)) - clock.millis()
            delay(untilMidnight.coerceAtLeast(1))
        }
    }.distinctUntilChanged()
}
