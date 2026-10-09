package com.dev.goodluckcy.brainup.domain.repository

import com.dev.goodluckcy.brainup.domain.model.DailyProgress
import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    /** 오늘의 진행 상황. 자정이 지나면 새 날짜로 바뀐다. */
    fun observeToday(): Flow<DailyProgress>

    fun observeStreak(): Flow<Int>

    /** 오늘을 포함한 최근 [days]일, 오래된 날짜부터. 기록이 없는 날도 포함한다. */
    fun observeRecentDays(days: Int): Flow<List<DailyProgress>>
}
