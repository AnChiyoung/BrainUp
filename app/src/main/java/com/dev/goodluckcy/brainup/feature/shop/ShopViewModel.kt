package com.dev.goodluckcy.brainup.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsEvent
import com.dev.goodluckcy.brainup.core.analytics.AnalyticsLogger
import com.dev.goodluckcy.brainup.domain.model.Inventory
import com.dev.goodluckcy.brainup.domain.model.ShopItem
import com.dev.goodluckcy.brainup.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val coins: Int = 0,
    val inventory: Inventory = Inventory(),
    /** 구매 확인 중인 아이템 */
    val pendingPurchase: ShopItem? = null,
) {
    fun canAfford(item: ShopItem): Boolean = coins >= item.price
    fun isMaxed(item: ShopItem): Boolean = inventory.quantityOf(item) >= item.maxQuantity
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    analytics: AnalyticsLogger,
) : ViewModel() {

    private val pendingPurchase = MutableStateFlow<ShopItem?>(null)
    private var purchasing = false

    val uiState: StateFlow<ShopUiState> = combine(
        coinRepository.observeBalance(),
        coinRepository.observeInventory(),
        pendingPurchase,
    ) { coins, inventory, pending ->
        ShopUiState(coins = coins, inventory = inventory, pendingPurchase = pending)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )

    init {
        analytics.log(AnalyticsEvent.shopView())
    }

    /** 가진 꾸미기 아이템은 바로 장착하고, 아니면 구매 확인을 띄운다. */
    fun onItemClick(item: ShopItem) {
        val state = uiState.value
        when {
            !item.isConsumable && state.inventory.owns(item) -> viewModelScope.launch { coinRepository.equip(item) }
            state.isMaxed(item) || !state.canAfford(item) -> Unit
            else -> pendingPurchase.value = item
        }
    }

    fun confirmPurchase() {
        val item = pendingPurchase.value ?: return
        if (purchasing) return
        purchasing = true
        viewModelScope.launch {
            try {
                coinRepository.purchase(item)
            } finally {
                purchasing = false
                pendingPurchase.value = null
            }
        }
    }

    fun cancelPurchase() {
        pendingPurchase.value = null
    }
}
