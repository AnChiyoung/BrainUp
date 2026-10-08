package com.dev.goodluckcy.brainup.data.repository.di

import com.dev.goodluckcy.brainup.data.repository.OfflineDailyChallengeRepository
import com.dev.goodluckcy.brainup.data.repository.OfflineGameRecordRepository
import com.dev.goodluckcy.brainup.domain.repository.DailyChallengeRepository
import com.dev.goodluckcy.brainup.domain.repository.GameRecordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGameRecordRepository(impl: OfflineGameRecordRepository): GameRecordRepository

    @Binds
    abstract fun bindDailyChallengeRepository(impl: OfflineDailyChallengeRepository): DailyChallengeRepository
}
