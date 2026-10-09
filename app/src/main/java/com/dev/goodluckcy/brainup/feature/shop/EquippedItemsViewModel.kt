package com.dev.goodluckcy.brainup.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.goodluckcy.brainup.domain.model.Inventory
import com.dev.goodluckcy.brainup.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 게임 화면처럼 꾸미기 아이템만 필요한 곳에서 장착 상태를 읽는다. */
@HiltViewModel
class EquippedItemsViewModel @Inject constructor(
    coinRepository: CoinRepository,
) : ViewModel() {
    val inventory: StateFlow<Inventory> = coinRepository.observeInventory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Inventory(),
    )
}
