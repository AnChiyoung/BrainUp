package com.dev.goodluckcy.brainup.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.LocalDateProvider
import com.dev.goodluckcy.brainup.data.local.BrainUpDatabase
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.domain.model.CoinRules
import com.dev.goodluckcy.brainup.domain.model.CoinSource
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.PurchaseResult
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import com.dev.goodluckcy.brainup.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class OfflineCoinRepositoryTest {

    private lateinit var database: BrainUpDatabase
    private lateinit var coins: OfflineCoinRepository
    private lateinit var dailyChallenge: OfflineDailyChallengeRepository
    private val events = mutableListOf<AnalyticsEvent>()
    private val clock = MutableClock(Instant.EPOCH)
    private val today = LocalDate.of(2026, 10, 9)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrainUpDatabase::class.java,
        ).build()
        val dateProvider = LocalDateProvider(clock)
        coins = OfflineCoinRepository(
            database = database,
            coinDao = database.coinDao(),
            dailyProgressDao = database.dailyProgressDao(),
            dateProvider = dateProvider,
            analytics = object : AnalyticsLogger {
                override fun log(event: AnalyticsEvent) {
                    events += event
                }
            },
        )
        dailyChallenge = OfflineDailyChallengeRepository(database.dailyProgressDao(), dateProvider)
        setDay(today)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun setDay(date: LocalDate) {
        clock.setLocal(LocalDateTime.of(date, LocalTime.NOON))
    }

    private suspend fun markCompleted(vararg dates: LocalDate) {
        dates.forEach {
            database.dailyProgressDao().upsert(
                DailyProgressEntity(
                    date = it.toString(),
                    completedMask = GameType.ALL_COMPLETED_MASK,
                    totalScore = 300,
                    completed = true,
                ),
            )
        }
    }

    private suspend fun giveCoins(amount: Int) {
        // 환영 선물 + 게임 보상으로 잔액을 채운다.
        coins.grantWelcomeIfNeeded()
        while (coins.observeBalance().first() < amount) {
            coins.rewardGame(isPersonalBest = true, dailyCompletedNow = false)
        }
    }

    @Test
    fun welcomeGiftIsGrantedOnce() = runTest {
        assertEquals(CoinRules.WELCOME, coins.grantWelcomeIfNeeded())
        assertNull(coins.grantWelcomeIfNeeded())
        assertEquals(CoinRules.WELCOME, coins.observeBalance().first())
    }

    @Test
    fun gameRewardIncludesPersonalBest() = runTest {
        val reward = coins.rewardGame(isPersonalBest = true, dailyCompletedNow = false)

        assertEquals(CoinRules.GAME_COMPLETE + CoinRules.PERSONAL_BEST, reward.total)
        assertEquals(0, reward.balanceBefore)
        assertEquals(reward.total, reward.balanceAfter)
        assertEquals(1, reward.rewardedGamesToday)
        assertTrue(events.any { it.name == "coin_earned" && it.params["source"] == CoinSource.PERSONAL_BEST.key })
    }

    @Test
    fun gameCompleteCoinsStopAtDailyLimit() = runTest {
        repeat(CoinRules.DAILY_GAME_LIMIT) { coins.rewardGame(isPersonalBest = false, dailyCompletedNow = false) }

        val overLimit = coins.rewardGame(isPersonalBest = false, dailyCompletedNow = false)
        assertTrue(overLimit.gameLimitReached)
        assertEquals(0, overLimit.total)
        assertEquals(CoinRules.DAILY_GAME_LIMIT * CoinRules.GAME_COMPLETE, coins.observeBalance().first())

        // 다음 날이면 다시 받을 수 있다.
        setDay(today.plusDays(1))
        assertEquals(CoinRules.GAME_COMPLETE, coins.rewardGame(isPersonalBest = false, dailyCompletedNow = false).total)
    }

    @Test
    fun streakBonusOnSeventhDay() = runTest {
        markCompleted(*(0L..6L).map { today.minusDays(it) }.toTypedArray())

        val reward = coins.rewardGame(isPersonalBest = false, dailyCompletedNow = true)
        assertEquals(CoinRules.GAME_COMPLETE + CoinRules.STREAK_BONUSES.getValue(7), reward.total)
    }

    @Test
    fun chestOpensOnceAfterDailyComplete() = runTest {
        assertNull(coins.claimChest(double = false))
        assertFalse(coins.observeChest().first().isReady)

        markCompleted(today)
        assertTrue(coins.observeChest().first().isReady)
        assertEquals(CoinRules.CHEST + CoinRules.CHEST_DOUBLE_BONUS, coins.claimChest(double = true))
        assertNull(coins.claimChest(double = false))

        val chest = coins.observeChest().first()
        assertTrue(chest.claimed)
        assertFalse(chest.isReady)
        assertEquals(CoinRules.CHEST + CoinRules.CHEST_DOUBLE_BONUS, coins.observeBalance().first())
    }

    @Test
    fun purchaseNeedsEnoughCoins() = runTest {
        assertEquals(PurchaseResult.InsufficientCoins, coins.purchase(ShopItem.BRAINY_MINT))
        assertEquals(0, coins.observeBalance().first())
    }

    @Test
    fun cosmeticIsOwnedOnceAndEquippedOnPurchase() = runTest {
        giveCoins(ShopItem.BRAINY_MINT.price)
        val before = coins.observeBalance().first()

        val result = coins.purchase(ShopItem.BRAINY_MINT)
        assertTrue(result is PurchaseResult.Success)
        assertEquals(before - ShopItem.BRAINY_MINT.price, coins.observeBalance().first())
        assertEquals(PurchaseResult.AlreadyOwned, coins.purchase(ShopItem.BRAINY_MINT))

        val inventory = coins.observeInventory().first()
        assertTrue(inventory.owns(ShopItem.BRAINY_MINT))
        assertEquals(ShopItem.BRAINY_MINT, inventory.equippedIn(ItemSlot.BRAINY_COLOR))

        coins.equip(ShopItem.BRAINY_PINK)
        assertEquals(ShopItem.BRAINY_PINK, coins.observeInventory().first().equippedIn(ItemSlot.BRAINY_COLOR))
        // 사지 않은 아이템은 장착되지 않는다.
        coins.equip(ShopItem.BRAINY_RAINBOW)
        assertEquals(ShopItem.BRAINY_PINK, coins.observeInventory().first().equippedIn(ItemSlot.BRAINY_COLOR))
    }

    @Test
    fun shieldsStackUpToMax() = runTest {
        giveCoins(ShopItem.SHIELD.price * 3)

        repeat(ShopItem.SHIELD.maxQuantity) {
            assertTrue(coins.purchase(ShopItem.SHIELD) is PurchaseResult.Success)
        }
        assertEquals(PurchaseResult.MaxQuantity, coins.purchase(ShopItem.SHIELD))
        assertEquals(ShopItem.SHIELD.maxQuantity, coins.observeInventory().first().shields)
    }

    @Test
    fun shieldKeepsStreakAfterMissedDay() = runTest {
        giveCoins(ShopItem.SHIELD.price)
        coins.purchase(ShopItem.SHIELD)
        // 3일 전 ~ 2일 전 완료, 어제는 놓침
        markCompleted(today.minusDays(3), today.minusDays(2))

        val use = coins.applyStreakShields()
        assertEquals(1, use?.used)
        assertEquals(2, use?.streakDays)
        assertEquals(0, use?.shieldsLeft)
        assertEquals(2, dailyChallenge.observeStreak().first())
        assertTrue(dailyChallenge.observeRecentDays(7).first().single { it.date == today.minusDays(1) }.shielded)

        // 이미 지켰으면 다시 쓰지 않는다.
        assertNull(coins.applyStreakShields())
    }

    @Test
    fun notEnoughShieldsLeavesStreakBroken() = runTest {
        giveCoins(ShopItem.SHIELD.price)
        coins.purchase(ShopItem.SHIELD)
        // 이틀을 놓쳤는데 방패는 하나뿐
        markCompleted(today.minusDays(3))

        assertNull(coins.applyStreakShields())
        assertEquals(1, coins.observeInventory().first().shields)
        assertEquals(0, dailyChallenge.observeStreak().first())
    }

    @Test
    fun noShieldUsedWithoutStreak() = runTest {
        giveCoins(ShopItem.SHIELD.price)
        coins.purchase(ShopItem.SHIELD)

        assertNull(coins.applyStreakShields())
        assertEquals(1, coins.observeInventory().first().shields)
    }
}
