package com.dev.goodluckcy.brainup.feature.home

import com.dev.goodluckcy.brainup.domain.model.ChestState
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.ShieldUse
import com.dev.goodluckcy.brainup.domain.model.ShopItem

data class HomeUiState(
    val completedGames: Set<GameType> = emptySet(),
    val todayScore: Int = 0,
    val streakDays: Int = 0,
    val coins: Int = 0,
    val shields: Int = 0,
    val brainy: ShopItem = ShopItem.defaultFor(ItemSlot.BRAINY_COLOR),
    val mapTheme: ShopItem = ShopItem.defaultFor(ItemSlot.MAP_THEME),
    val chestClaimed: Boolean = false,
    /** 지금 띄울 팝업. 여러 개면 하나씩 차례로 띄운다. */
    val dialog: HomeDialog? = null,
) {
    val completedCount: Int get() = completedGames.size
    val totalCount: Int get() = GameType.entries.size
    val isDailyCompleted: Boolean get() = completedCount == totalCount

    /** 오늘의 모험을 끝냈고 아직 보물상자를 열지 않았다. */
    val isChestReady: Boolean get() = ChestState(completedCount, totalCount, chestClaimed).isReady
}

sealed interface HomeDialog {
    data class Welcome(val coins: Int) : HomeDialog
    data class ShieldUsed(val use: ShieldUse) : HomeDialog
    data object Chest : HomeDialog
}
