package com.dev.goodluckcy.brainup.core.common.di

import com.dev.goodluckcy.brainup.core.common.MonotonicClock
import com.dev.goodluckcy.brainup.core.common.SystemMonotonicClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.random.Random

/** 화면 수명과 무관하게 끝까지 실행해야 하는 작업(기록 저장 등)용 스코프 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    abstract fun bindMonotonicClock(impl: SystemMonotonicClock): MonotonicClock

    companion object {
        @Provides
        fun provideRandom(): Random = Random.Default

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()

        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
