package com.dev.goodluckcy.brainup.data.repository

import androidx.room.withTransaction
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.core.common.LocalDateProvider
import com.dev.goodluckcy.brainup.data.local.BrainUpDatabase
import com.dev.goodluckcy.brainup.data.local.dao.CoinDao
import com.dev.goodluckcy.brainup.data.local.dao.DailyProgressDao
import com.dev.goodluckcy.brainup.data.local.entity.CoinTransactionEntity
import com.dev.goodluckcy.brainup.data.local.entity.DailyProgressEntity
import com.dev.goodluckcy.brainup.data.local.entity.EquippedItemEntity
import com.dev.goodluckcy.brainup.data.local.entity.InventoryEntity
import com.dev.goodluckcy.brainup.domain.model.ChestState
import com.dev.goodluckcy.brainup.domain.model.CoinGain
import com.dev.goodluckcy.brainup.domain.model.CoinReward
import com.dev.goodluckcy.brainup.domain.model.CoinRules
import com.dev.goodluckcy.brainup.domain.model.CoinSource
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.Inventory
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.PurchaseResult
import com.dev.goodluckcy.brainup.domain.model.ShieldUse
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import com.dev.goodluckcy.brainup.domain.model.Streak
import com.dev.goodluckcy.brainup.domain.repository.CoinRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineCoinRepository @Inject constructor(
    private val database: BrainUpDatabase,
    private val coinDao: CoinDao,
    private val dailyProgressDao: DailyProgressDao,
    private val dateProvider: LocalDateProvider,
    private val analytics: AnalyticsLogger,
) : CoinRepository {

    override fun observeBalance(): Flow<Int> = coinDao.observeBalance()

    override fun observeInventory(): Flow<Inventory> =
        combine(coinDao.observeInventory(), coinDao.observeEquipped()) { owned, equipped ->
            Inventory(
                quantities = owned.mapNotNull { row -> ShopItem.fromId(row.itemId)?.let { it to row.quantity } }.toMap(),
                equipped = equipped.mapNotNull { row ->
                    val slot = ItemSlot.entries.find { it.key == row.slot } ?: return@mapNotNull null
                    val item = ShopItem.fromId(row.itemId) ?: return@mapNotNull null
                    slot to item
                }.toMap(),
            )
        }

    override fun observeChest(): Flow<ChestState> =
        dateProvider.observeToday().flatMapLatest { today ->
            combine(
                dailyProgressDao.observe(today.toString()),
                coinDao.observeCountBySourceOn(CoinSource.CHEST.key, today.toString()),
            ) { progress, claimedCount ->
                ChestState(
                    completedCount = Integer.bitCount(progress?.completedMask ?: 0),
                    totalCount = GameType.entries.size,
                    claimed = claimedCount > 0,
                )
            }
        }

    override suspend fun grantWelcomeIfNeeded(): Int? = database.withTransaction {
        if (coinDao.countBySource(CoinSource.WELCOME.key) > 0) return@withTransaction null
        earn(CoinSource.WELCOME, CoinRules.WELCOME)
        CoinRules.WELCOME
    }

    override suspend fun rewardGame(isPersonalBest: Boolean, dailyCompletedNow: Boolean): CoinReward =
        database.withTransaction {
            val today = dateProvider.today()
            val gains = mutableListOf<CoinGain>()

            // 같은 게임을 무한 반복해 코인을 찍어내지 않도록 하루 판수를 제한한다.
            val rewardedGames = coinDao.countBySourceOn(CoinSource.GAME_COMPLETE.key, today.toString())
            val limitReached = rewardedGames >= CoinRules.DAILY_GAME_LIMIT
            if (!limitReached) gains += CoinGain(CoinSource.GAME_COMPLETE, CoinRules.GAME_COMPLETE)
            if (isPersonalBest) gains += CoinGain(CoinSource.PERSONAL_BEST, CoinRules.PERSONAL_BEST)
            if (dailyCompletedNow) {
                val streak = currentStreak(today)
                CoinRules.STREAK_BONUSES[streak]?.let { gains += CoinGain(CoinSource.STREAK_BONUS, it) }
            }
            gains.forEach { earn(it.source, it.amount) }

            CoinReward(
                gains = gains,
                balanceAfter = coinDao.balance(),
                rewardedGamesToday = if (limitReached) rewardedGames else rewardedGames + 1,
                gameLimitReached = limitReached,
            )
        }

    override suspend fun claimChest(double: Boolean): Int? = database.withTransaction {
        val today = dateProvider.today().toString()
        val progress = dailyProgressDao.get(today)
        val completed = progress?.completed == true
        val claimed = coinDao.countBySourceOn(CoinSource.CHEST.key, today) > 0
        if (!completed || claimed) return@withTransaction null
        earn(CoinSource.CHEST, CoinRules.CHEST)
        if (double) earn(CoinSource.CHEST_DOUBLE, CoinRules.CHEST_DOUBLE_BONUS)
        CoinRules.CHEST + if (double) CoinRules.CHEST_DOUBLE_BONUS else 0
    }

    override suspend fun purchase(item: ShopItem): PurchaseResult = database.withTransaction {
        val owned = if (item.isDefault) 1 else coinDao.quantity(item.id) ?: 0
        when {
            !item.isConsumable && owned > 0 -> return@withTransaction PurchaseResult.AlreadyOwned
            owned >= item.maxQuantity -> return@withTransaction PurchaseResult.MaxQuantity
            coinDao.balance() < item.price -> return@withTransaction PurchaseResult.InsufficientCoins
        }
        insert(CoinSource.PURCHASE, -item.price, itemId = item.id)
        coinDao.upsertInventory(InventoryEntity(item.id, owned + 1))
        // 꾸미기 아이템은 사자마자 장착한다.
        item.slot?.let { coinDao.upsertEquipped(EquippedItemEntity(it.key, item.id)) }
        analytics.log(AnalyticsEvent.coinSpent(item.id, item.price))
        PurchaseResult.Success(item, coinDao.balance())
    }

    override suspend fun equip(item: ShopItem) {
        val slot = item.slot ?: return
        database.withTransaction {
            val owned = item.isDefault || (coinDao.quantity(item.id) ?: 0) > 0
            if (owned) coinDao.upsertEquipped(EquippedItemEntity(slot.key, item.id))
        }
    }

    override suspend fun applyStreakShields(): ShieldUse? = database.withTransaction {
        val today = dateProvider.today()
        val days = dailyProgressDao.streakDays()
        val completed = days.filter { it.completed }.map { LocalDate.parse(it.date) }.toSet()
        val shielded = days.filter { it.shielded }.map { LocalDate.parse(it.date) }.toSet()
        val yesterday = today.minusDays(1)
        if (yesterday in completed || yesterday in shielded) return@withTransaction null

        // 오늘 이전에 기록이 이어진 마지막 날과, 그 뒤로 놓친 날들
        val lastDay = (completed + shielded).filter { it < today }.maxOrNull() ?: return@withTransaction null
        val streakAtLast = Streak.countFrom(lastDay, completed, shielded)
        if (streakAtLast == 0) return@withTransaction null
        val missed = ChronoUnit.DAYS.between(lastDay, today).toInt() - 1
        val shields = coinDao.quantity(ShopItem.SHIELD.id) ?: 0
        if (missed <= 0 || missed > shields) return@withTransaction null

        for (offset in 1..missed) {
            val date = lastDay.plusDays(offset.toLong()).toString()
            val existing = dailyProgressDao.get(date)
            dailyProgressDao.upsert(
                existing?.copy(shielded = true)
                    ?: DailyProgressEntity(date = date, completedMask = 0, totalScore = 0, completed = false, shielded = true),
            )
        }
        coinDao.upsertInventory(InventoryEntity(ShopItem.SHIELD.id, shields - missed))
        analytics.log(AnalyticsEvent.streakShieldUsed(streakAtLast))
        ShieldUse(used = missed, streakDays = streakAtLast, shieldsLeft = shields - missed)
    }

    private suspend fun currentStreak(today: LocalDate): Int {
        val days = dailyProgressDao.streakDays()
        return Streak.current(
            completedDates = days.filter { it.completed }.map { LocalDate.parse(it.date) },
            today = today,
            shieldedDates = days.filter { it.shielded }.map { LocalDate.parse(it.date) },
        )
    }

    private suspend fun earn(source: CoinSource, amount: Int) {
        insert(source, amount)
        analytics.log(AnalyticsEvent.coinEarned(source.key, amount))
    }

    private suspend fun insert(source: CoinSource, amount: Int, itemId: String? = null) {
        val now = dateProvider.nowMs()
        coinDao.insert(
            CoinTransactionEntity(
                amount = amount,
                source = source.key,
                itemId = itemId,
                date = dateProvider.dateOf(now).toString(),
                createdAt = now,
            ),
        )
    }
}
