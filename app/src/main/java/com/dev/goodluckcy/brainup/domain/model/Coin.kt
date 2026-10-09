package com.dev.goodluckcy.brainup.domain.model

/** 코인 규칙 (docs/design/coin-economy.md) */
object CoinRules {
    const val WELCOME = 100
    const val GAME_COMPLETE = 10
    /** 게임 완료 코인을 받을 수 있는 하루 최대 판수 */
    const val DAILY_GAME_LIMIT = 20
    const val PERSONAL_BEST = 20
    const val CHEST = 50
    const val CHEST_DOUBLE_BONUS = 50
    /** 연속 일수 → 달성 보너스 */
    val STREAK_BONUSES = mapOf(7 to 100, 30 to 300)
}

/** 코인 거래 출처. [key]는 DB와 Analytics에 쓰는 고정 값이다. */
enum class CoinSource(val key: String) {
    WELCOME("welcome"),
    GAME_COMPLETE("game_complete"),
    PERSONAL_BEST("personal_best"),
    CHEST("chest"),
    CHEST_DOUBLE("chest_double"),
    STREAK_BONUS("streak_bonus"),
    PURCHASE("purchase"),
}

/** 한 번의 지급 항목 */
data class CoinGain(val source: CoinSource, val amount: Int)

/** 게임 한 판으로 받은 코인 */
data class CoinReward(
    val gains: List<CoinGain>,
    val balanceAfter: Int,
    /** 오늘 게임 완료 코인을 받은 판수(이번 판 포함) */
    val rewardedGamesToday: Int,
    /** 하루 판수 제한에 걸려 게임 완료 코인을 못 받았으면 true */
    val gameLimitReached: Boolean,
) {
    val total: Int get() = gains.sumOf { it.amount }
    val balanceBefore: Int get() = balanceAfter - total
}

/** 오늘의 보물상자 상태 */
data class ChestState(
    val completedCount: Int,
    val totalCount: Int,
    val claimed: Boolean,
) {
    val isReady: Boolean get() = completedCount >= totalCount && !claimed
}

/** 상점 아이템 종류. 꾸미기 아이템은 슬롯마다 하나를 장착한다. */
enum class ItemSlot(val key: String) {
    BRAINY_COLOR("brainy_color"),
    TILE_SKIN("tile_skin"),
    MAP_THEME("map_theme"),
}

/**
 * 상점 아이템 목록. [id]는 DB에 저장되는 고정 값이라 바꾸면 안 된다.
 * 가격이 0인 아이템은 기본으로 보유한다.
 */
enum class ShopItem(
    val id: String,
    val price: Int,
    val slot: ItemSlot?,
    val maxQuantity: Int = 1,
) {
    SHIELD("streak_shield", 150, slot = null, maxQuantity = 2),

    BRAINY_PINK("brainy_pink", 0, ItemSlot.BRAINY_COLOR),
    BRAINY_MINT("brainy_mint", 200, ItemSlot.BRAINY_COLOR),
    BRAINY_SKY("brainy_sky", 300, ItemSlot.BRAINY_COLOR),
    BRAINY_YELLOW("brainy_yellow", 300, ItemSlot.BRAINY_COLOR),
    BRAINY_RAINBOW("brainy_rainbow", 500, ItemSlot.BRAINY_COLOR),

    TILE_GEM("tile_gem", 0, ItemSlot.TILE_SKIN),
    TILE_FRUIT("tile_fruit", 400, ItemSlot.TILE_SKIN),
    TILE_STAR("tile_star", 400, ItemSlot.TILE_SKIN),

    THEME_NIGHT("theme_night", 0, ItemSlot.MAP_THEME),
    THEME_OCEAN("theme_ocean", 600, ItemSlot.MAP_THEME),
    THEME_SPACE("theme_space", 1000, ItemSlot.MAP_THEME),
    ;

    val isConsumable: Boolean get() = slot == null
    val isDefault: Boolean get() = price == 0

    companion object {
        fun fromId(id: String): ShopItem? = entries.find { it.id == id }
        fun defaultFor(slot: ItemSlot): ShopItem = entries.first { it.slot == slot && it.isDefault }
        fun inSlot(slot: ItemSlot): List<ShopItem> = entries.filter { it.slot == slot }
    }
}

/** 보유·장착 현황 */
data class Inventory(
    val quantities: Map<ShopItem, Int> = emptyMap(),
    val equipped: Map<ItemSlot, ShopItem> = emptyMap(),
) {
    fun quantityOf(item: ShopItem): Int = if (item.isDefault) 1 else quantities[item] ?: 0
    fun owns(item: ShopItem): Boolean = quantityOf(item) > 0
    fun equippedIn(slot: ItemSlot): ShopItem = equipped[slot] ?: ShopItem.defaultFor(slot)
    val shields: Int get() = quantityOf(ShopItem.SHIELD)
}

sealed interface PurchaseResult {
    data class Success(val item: ShopItem, val balanceAfter: Int) : PurchaseResult
    data object InsufficientCoins : PurchaseResult
    data object AlreadyOwned : PurchaseResult
    data object MaxQuantity : PurchaseResult
}

/** 불꽃 방패가 연속 기록을 지킨 결과 */
data class ShieldUse(val used: Int, val streakDays: Int, val shieldsLeft: Int)
