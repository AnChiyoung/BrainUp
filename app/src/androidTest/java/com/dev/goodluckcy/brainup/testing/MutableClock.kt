package com.dev.goodluckcy.brainup.testing

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** 테스트에서 시각을 바꿀 수 있는 Clock */
class MutableClock(
    var instant: Instant,
    private val zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
) : Clock() {

    fun setLocal(dateTime: LocalDateTime) {
        instant = dateTime.atZone(zoneId).toInstant()
    }

    override fun instant(): Instant = instant
    override fun getZone(): ZoneId = zoneId
    override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)
}
