package com.dev.goodluckcy.brainup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dev.goodluckcy.brainup.data.local.entity.CoinTransactionEntity
import com.dev.goodluckcy.brainup.data.local.entity.EquippedItemEntity
import com.dev.goodluckcy.brainup.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {

    @Insert
    suspend fun insert(transaction: CoinTransactionEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transaction")
    suspend fun balance(): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transaction")
    fun observeBalance(): Flow<Int>

    @Query("SELECT COUNT(*) FROM coin_transaction WHERE source = :source")
    suspend fun countBySource(source: String): Int

    @Query("SELECT COUNT(*) FROM coin_transaction WHERE source = :source AND date = :date")
    suspend fun countBySourceOn(source: String, date: String): Int

    @Query("SELECT COUNT(*) FROM coin_transaction WHERE source = :source AND date = :date")
    fun observeCountBySourceOn(source: String, date: String): Flow<Int>

    @Query("SELECT quantity FROM inventory WHERE itemId = :itemId")
    suspend fun quantity(itemId: String): Int?

    @Upsert
    suspend fun upsertInventory(item: InventoryEntity)

    @Query("SELECT * FROM inventory")
    fun observeInventory(): Flow<List<InventoryEntity>>

    @Upsert
    suspend fun upsertEquipped(item: EquippedItemEntity)

    @Query("SELECT * FROM equipped_item")
    fun observeEquipped(): Flow<List<EquippedItemEntity>>
}
