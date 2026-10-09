package com.dev.goodluckcy.brainup.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.designsystem.brainyBackground
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.component.CoinDot
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameDialog
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.component.tabBarPadding
import com.dev.goodluckcy.brainup.core.designsystem.formatCoins
import com.dev.goodluckcy.brainup.core.designsystem.nameRes
import com.dev.goodluckcy.brainup.core.designsystem.skyPalette
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.Night
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.tileSkin
import com.dev.goodluckcy.brainup.domain.model.Inventory
import com.dev.goodluckcy.brainup.domain.model.ItemSlot
import com.dev.goodluckcy.brainup.domain.model.ShopItem

@Composable
fun ShopScreen(viewModel: ShopViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShopContent(uiState = uiState, onItemClick = viewModel::onItemClick)
    uiState.pendingPurchase?.let { item ->
        PurchaseDialog(
            item = item,
            coins = uiState.coins,
            onConfirm = viewModel::confirmPurchase,
            onCancel = viewModel::cancelPurchase,
        )
    }
}

@Composable
private fun ShopContent(
    uiState: ShopUiState,
    onItemClick: (ShopItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky(variant = 1)
            .statusBarsPadding()
            .tabBarPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.shop_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            HudChip(text = formatCoins(uiState.coins)) { CoinDot(size = 20.dp) }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShieldCard(uiState = uiState, onClick = { onItemClick(ShopItem.SHIELD) })
            SlotSection(R.string.shop_section_brainy, ItemSlot.BRAINY_COLOR, uiState, onItemClick)
            SlotSection(R.string.shop_section_tile, ItemSlot.TILE_SKIN, uiState, onItemClick)
            SlotSection(R.string.shop_section_theme, ItemSlot.MAP_THEME, uiState, onItemClick)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
    )
}

@Composable
private fun ShieldCard(uiState: ShopUiState, onClick: () -> Unit) {
    val item = ShopItem.SHIELD
    val owned = uiState.inventory.quantityOf(item)
    SectionTitle(stringResource(R.string.shop_section_shield))
    GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ItemPreview(item = item, modifier = Modifier.size(58.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(item.nameRes), style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(stringResource(R.string.shop_shield_desc), style = MaterialTheme.typography.bodyMedium, color = Lavender)
                Text(
                    text = stringResource(R.string.shop_shield_owned, owned, item.maxQuantity),
                    style = MaterialTheme.typography.labelMedium,
                    color = Sky,
                )
            }
            val maxed = uiState.isMaxed(item)
            GameButton(
                onClick = onClick,
                enabled = !maxed && uiState.canAfford(item),
                modifier = Modifier.height(48.dp),
                depth = 4.dp,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                if (maxed) {
                    Text(stringResource(R.string.shop_max), style = MaterialTheme.typography.titleMedium, color = Ink)
                } else {
                    CoinDot(size = 16.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(formatCoins(item.price), style = MaterialTheme.typography.titleMedium, color = Ink)
                }
            }
        }
    }
}

@Composable
private fun SlotSection(
    titleRes: Int,
    slot: ItemSlot,
    uiState: ShopUiState,
    onItemClick: (ShopItem) -> Unit,
) {
    SectionTitle(stringResource(titleRes))
    ShopItem.inSlot(slot).chunked(COLUMNS).forEach { rowItems ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            rowItems.forEach { item ->
                ItemCard(
                    item = item,
                    inventory = uiState.inventory,
                    canAfford = uiState.canAfford(item),
                    onClick = { onItemClick(item) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(COLUMNS - rowItems.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun ItemCard(
    item: ShopItem,
    inventory: Inventory,
    canAfford: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val equipped = item.slot?.let { inventory.equippedIn(it) } == item
    val owned = inventory.owns(item)
    val name = stringResource(item.nameRes)
    GamePanel(
        modifier = modifier,
        color = if (equipped) Night else NightDeep,
        depth = 4.dp,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (equipped) Modifier.border(3.dp, Sun, RoundedCornerShape(18.dp)) else Modifier)
                .semantics { selected = equipped }
                .clickable(enabled = owned || canAfford, role = Role.Button, onClickLabel = name, onClick = onClick)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ItemPreview(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .alpha(if (owned || canAfford) 1f else 0.55f),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            ItemStatus(equipped = equipped, owned = owned, canAfford = canAfford, price = item.price)
        }
    }
}

@Composable
private fun ItemStatus(equipped: Boolean, owned: Boolean, canAfford: Boolean, price: Int) {
    when {
        // 장착 중: 노란 배지 + 체크
        equipped -> Row(
            modifier = Modifier
                .background(Sun, RoundedCornerShape(999.dp))
                .padding(start = 6.dp, end = 9.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            GameIcon(icon = GameIcons.Check, size = 13.dp, tint = Ink, strokeWidth = 3.4f)
            Text(stringResource(R.string.shop_equipped), style = MaterialTheme.typography.labelMedium, color = Ink)
        }
        // 가졌지만 끼지 않음: 누르면 바로 바뀐다는 걸 알 수 있게 버튼 모양으로
        owned -> Text(
            text = stringResource(R.string.shop_equip),
            modifier = Modifier
                .border(2.dp, Color.White, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
        else -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (canAfford) {
                CoinDot(size = 14.dp)
                Text(formatCoins(price), style = MaterialTheme.typography.labelLarge, color = Sun)
            } else {
                GameIcon(
                    icon = GameIcons.Lock,
                    size = 14.dp,
                    tint = Lavender,
                    strokeWidth = 2.4f,
                    contentDescription = stringResource(R.string.shop_not_enough),
                )
                Text(formatCoins(price), style = MaterialTheme.typography.labelLarge, color = Lavender)
            }
        }
    }
}

/** 아이템이 실제로 어떻게 보이는지 미리 보여 준다. */
@Composable
private fun ItemPreview(item: ShopItem, modifier: Modifier = Modifier) {
    when (item.slot) {
        ItemSlot.BRAINY_COLOR -> Box(
            modifier = modifier
                .chunky(color = Color.Transparent, shape = RoundedCornerShape(22.dp), depth = 0.dp)
                .brainyBackground(item, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.fillMaxSize(0.7f))
        }
        ItemSlot.TILE_SKIN -> {
            val skin = item.tileSkin
            Box(
                modifier = modifier
                    .background(NightDeeper, RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .chunky(color = skin.lit, shape = skin.shape, depth = 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                GameIcon(icon = skin.litIcon, size = 22.dp, tint = skin.litIconTint, fill = skin.litIconFill, strokeWidth = 1.4f)
            }
        }
        ItemSlot.MAP_THEME -> {
            val sky = item.skyPalette
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(18.dp))
                    .nightSky(variant = 0, ground = sky.ground, planet = sky.planet, accentPlanet = sky.accentPlanet)
                    .border(3.dp, Ink, RoundedCornerShape(18.dp)),
            )
        }
        null -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            GameIcon(icon = GameIcons.Shield, size = 54.dp, tint = Ink, fill = Sky, strokeWidth = 1.8f)
            GameIcon(icon = GameIcons.Flame, size = 24.dp, tint = Ink, fill = Orange, strokeWidth = 1.4f)
        }
    }
}

@Composable
private fun PurchaseDialog(
    item: ShopItem,
    coins: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val name = stringResource(item.nameRes)
    GameDialog(onDismissRequest = onCancel) {
        ItemPreview(item = item, modifier = Modifier.size(96.dp))
        Text(
            text = stringResource(R.string.shop_buy_title, name),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (item.isConsumable) {
                stringResource(R.string.shop_buy_desc_shield, item.maxQuantity)
            } else {
                stringResource(R.string.shop_buy_desc_cosmetic)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Lavender,
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NightDeep, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReceiptLine(stringResource(R.string.shop_buy_balance), formatCoins(coins), Color.White)
            ReceiptLine(stringResource(R.string.shop_buy_price), "- " + formatCoins(item.price), Orange)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(NightLight),
            )
            ReceiptLine(stringResource(R.string.shop_buy_after), formatCoins(coins - item.price), Sun)
        }
        DialogAction(
            onClick = onConfirm,
            color = Sun,
            shadowColor = Ink,
        ) {
            Text(
                text = stringResource(R.string.shop_buy_confirm, formatCoins(item.price)),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        }
        DialogAction(onClick = onCancel, color = NightDeep, shadowColor = NightDeeper) {
            Text(stringResource(R.string.shop_buy_cancel), style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

@Composable
private fun DialogAction(
    onClick: () -> Unit,
    color: Color,
    shadowColor: Color,
    height: Dp = 58.dp,
    content: @Composable RowScope.() -> Unit,
) {
    GameButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        color = color,
        shadowColor = shadowColor,
        content = content,
    )
}

@Composable
private fun ReceiptLine(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = Lavender)
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}

private const val COLUMNS = 3

@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun ShopContentPreview() {
    BrainUpTheme {
        ShopContent(
            uiState = ShopUiState(
                coins = 1270,
                inventory = Inventory(
                    quantities = mapOf(ShopItem.SHIELD to 1, ShopItem.BRAINY_MINT to 1),
                    equipped = mapOf(ItemSlot.BRAINY_COLOR to ShopItem.BRAINY_MINT),
                ),
            ),
            onItemClick = {},
        )
    }
}
