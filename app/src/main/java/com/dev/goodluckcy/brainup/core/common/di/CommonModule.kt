package com.dev.goodluckcy.brainup.core.common.di

import com.dev.goodluckcy.brainup.core.common.MonotonicClock
import com.dev.goodluckcy.brainup.core.common.SystemMonotonicClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    abstract fun bindMonotonicClock(impl: SystemMonotonicClock): MonotonicClock

    companion object {
        @Provides
        fun provideRandom(): Random = Random.Default
    }
}
