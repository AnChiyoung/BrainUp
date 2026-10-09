package com.dev.goodluckcy.brainup.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProgressDao {

    @Upsert
    suspend fun upsert(progress: DailyProgressEntity)

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun get(date: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    fun observe(date: String): Flow<DailyProgressEntity?>

    /** YYYY-MM-DD 문자열은 사전순 비교가 날짜순과 같다. */
    @Query("SELECT * FROM daily_progress WHERE date >= :fromDate AND date <= :toDate ORDER BY date")
    fun observeRange(fromDate: String, toDate: String): Flow<List<DailyProgressEntity>>

    /** 연속 기록 계산용: 완료했거나 방패로 지킨 날 */
    @Query("SELECT * FROM daily_progress WHERE completed = 1 OR shielded = 1 ORDER BY date DESC")
    fun observeStreakDays(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE completed = 1 OR shielded = 1 ORDER BY date DESC")
    suspend fun streakDays(): List<DailyProgressEntity>
}
