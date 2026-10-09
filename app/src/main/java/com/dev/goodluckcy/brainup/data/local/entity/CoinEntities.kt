package com.dev.goodluckcy.brainup.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 코인 증감 내역. 잔액은 amount의 합이다. */
@Entity(
    tableName = "coin_transaction",
    indices = [Index("source"), Index("date")],
)
data class CoinTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 지급은 양수, 사용은 음수 */
    val amount: Int,
    /** CoinSource.key */
    val source: String,
    /** 구매·연속 보너스 등 관련 아이템/값 */
    val itemId: String? = null,
    /** 로컬 날짜 YYYY-MM-DD (하루 제한 계산용) */
    val date: String,
    val createdAt: Long,
)

/** 보유 아이템 수량 (ShopItem.id) */
@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val quantity: Int,
)

/** 슬롯별 장착 아이템 (ItemSlot.key → ShopItem.id) */
@Entity(tableName = "equipped_item")
data class EquippedItemEntity(
    @PrimaryKey val slot: String,
    val itemId: String,
)
