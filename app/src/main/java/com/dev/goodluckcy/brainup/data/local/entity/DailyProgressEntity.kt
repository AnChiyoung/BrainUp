package com.dev.goodluckcy.brainup.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    /** 로컬 날짜 YYYY-MM-DD */
    @PrimaryKey val date: String,
    /** 완료한 게임 비트마스크(GameType.bit) */
    val completedMask: Int,
    /** 그날 게임별 최고 점수의 합 */
    val totalScore: Int,
    val completed: Boolean,
    /** 불꽃 방패로 연속 기록을 지킨 날 (DB v2) */
    @ColumnInfo(defaultValue = "0") val shielded: Boolean = false,
)
