package com.dev.goodluckcy.brainup.domain.repository

import com.dev.goodluckcy.brainup.domain.model.BestRecord
import com.dev.goodluckcy.brainup.domain.model.GameResult
import com.dev.goodluckcy.brainup.domain.model.RecordOutcome
import kotlinx.coroutines.flow.Flow

interface GameRecordRepository {
    /** 게임 결과를 저장하고 오늘의 도전 진행 상황을 갱신한다. */
    suspend fun record(result: GameResult): RecordOutcome

    fun observeBestRecords(): Flow<List<BestRecord>>
}
