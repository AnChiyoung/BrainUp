package com.dev.goodluckcy.brainup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.dao.GameRecordDao
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.data.local.entity.GameRecordEntity

@Database(
    entities = [GameRecordEntity::class, DailyProgressEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BrainUpDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun dailyProgressDao(): DailyProgressDao

    companion object {
        const val NAME = "brainup.db"
    }
}
