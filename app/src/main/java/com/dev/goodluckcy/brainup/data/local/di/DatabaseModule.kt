package com.dev.goodluckcy.brainup.data.local.di

import android.content.Context
import androidx.room.Room
import com.dev.goodluckcy.brainup.data.local.BrainUpDatabase
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.dao.GameRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrainUpDatabase =
        Room.databaseBuilder(context, BrainUpDatabase::class.java, BrainUpDatabase.NAME).build()

    @Provides
    fun provideGameRecordDao(database: BrainUpDatabase): GameRecordDao = database.gameRecordDao()

    @Provides
    fun provideDailyProgressDao(database: BrainUpDatabase): DailyProgressDao = database.dailyProgressDao()
}
