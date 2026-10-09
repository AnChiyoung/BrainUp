package com.dev.goodluckcy.brainup.domain.repository

import com.dev.goodluckcy.brainup.domain.model.ChestState
import com.dev.goodluckcy.brainup.domain.model.CoinReward
import com.dev.goodluckcy.brainup.domain.model.Inventory
import com.dev.goodluckcy.brainup.domain.model.PurchaseResult
import com.dev.goodluckcy.brainup.domain.model.ShieldUse
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun observeBalance(): Flow<Int>

    fun observeInventory(): Flow<Inventory>

    /** 오늘의 보물상자. 자정이 지나면 새 날짜 기준으로 바뀐다. */
    fun observeChest(): Flow<ChestState>

    /** 첫 실행이면 환영 선물을 주고 지급액을, 이미 받았으면 null을 돌려준다. */
    suspend fun grantWelcomeIfNeeded(): Int?

    /**
     * 게임 한 판의 코인을 지급한다(게임 결과 저장 후 호출).
     * @param dailyCompletedNow 이번 판으로 오늘의 도전이 처음 완료됐는지(연속 보너스 판단)
     */
    suspend fun rewardGame(isPersonalBest: Boolean, dailyCompletedNow: Boolean): CoinReward

    /** 오늘의 보물상자를 연다. 열 수 없으면 null, 열면 받은 코인. */
    suspend fun claimChest(double: Boolean): Int?

    suspend fun purchase(item: ShopItem): PurchaseResult

    /** 보유한 꾸미기 아이템을 장착한다. */
    suspend fun equip(item: ShopItem)

    /**
     * 어제(또는 그 이전) 오늘의 도전을 놓쳐 연속 기록이 끊길 상황이면 불꽃 방패를 써서 지킨다.
     * 지킬 필요가 없거나 방패가 모자라면 null.
     */
    suspend fun applyStreakShields(): ShieldUse?
}
