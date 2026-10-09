package com.dev.goodluckcy.brainup.feature.home

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.analytics.RewardType
import com.dev.goodluckcy.brainup.core.designsystem.brainyBackground
import com.dev.goodluckcy.brainup.core.designsystem.component.AdTag
import com.dev.goodluckcy.brainup.core.designsystem.component.BrainyFace
import com.dev.goodluckcy.brainup.core.designsystem.component.CoinDot
import com.dev.goodluckcy.brainup.core.designsystem.component.GameButton
import com.dev.goodluckcy.brainup.core.designsystem.component.GameDialog
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcon
import com.dev.goodluckcy.brainup.core.designsystem.component.GameIcons
import com.dev.goodluckcy.brainup.core.designsystem.component.HudChip
import com.dev.goodluckcy.brainup.core.designsystem.component.RibbonBanner
import com.dev.goodluckcy.brainup.core.designsystem.component.TreasureChest
import com.dev.goodluckcy.brainup.core.designsystem.component.chunky
import com.dev.goodluckcy.brainup.core.designsystem.formatCoins
import com.dev.goodluckcy.brainup.core.designsystem.theme.Ink
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeep
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightDeeper
import com.dev.goodluckcy.brainup.core.designsystem.theme.Orange
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sky
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun
import com.dev.goodluckcy.brainup.core.designsystem.theme.SunDark
import com.dev.goodluckcy.brainup.domain.model.CoinRules
import com.dev.goodluckcy.brainup.domain.model.GameType
import com.dev.goodluckcy.brainup.domain.model.ShieldUse
import com.dev.goodluckcy.brainup.domain.model.ShopItem

@Composable
internal fun HomeDialogHost(
    uiState: HomeUiState,
    onClaimChest: (double: Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOpenShop: () -> Unit,
) {
    when (val dialog = uiState.dialog) {
        is HomeDialog.Welcome -> WelcomeDialog(coins = dialog.coins, brainy = uiState.brainy, onDismiss = onDismiss)
        is HomeDialog.ShieldUsed -> ShieldUsedDialog(
            use = dialog.use,
            onDismiss = onDismiss,
            onOpenShop = {
                onDismiss()
                onOpenShop()
            },
        )
        HomeDialog.Chest -> ChestDialog(onClaim = onClaimChest)
        null -> Unit
    }
}

/** 큰 코인 + 획득량 */
@Composable
private fun CoinGainLabel(amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CoinDot(size = 34.dp)
        Text(
            text = stringResource(R.string.coin_plus, formatCoins(amount)),
            style = MaterialTheme.typography.displaySmall,
            color = Sun,
        )
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    shadowColor: Color = Ink,
    leading: (@Composable () -> Unit)? = null,
) {
    GameButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        color = color,
        shadowColor = shadowColor,
    ) {
        leading?.let {
            it()
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleLarge, color = contentColor)
    }
}

@Composable
private fun ChestDialog(onClaim: (double: Boolean) -> Unit) {
    val adServices = LocalAdServices.current
    val activity = LocalActivity.current
    val adLoaded = adServices?.rewarded?.isLoaded?.collectAsStateWithLifecycle()?.value == true
    // 상자는 반드시 버튼으로 받게 한다(바깥 터치로 닫혀 보상을 놓치지 않도록).
    GameDialog(onDismissRequest = {}, dismissOnClickOutside = false) {
        RibbonBanner(text = stringResource(R.string.chest_title), color = Sun, tailColor = SunDark, fontSize = 24)
        // 열린 뚜껑이 위로 솟으므로 리본과 겹치지 않게 띄운다.
        TreasureChest(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(width = 110.dp, height = 84.dp),
            open = true,
        )
        CoinGainLabel(CoinRules.CHEST)
        Text(
            text = stringResource(R.string.chest_desc, GameType.entries.size),
            style = MaterialTheme.typography.bodyLarge,
            color = Lavender,
        )
        if (adServices != null && activity != null && adLoaded) {
            DialogButton(
                text = stringResource(R.string.chest_double, CoinRules.CHEST + CoinRules.CHEST_DOUBLE_BONUS),
                onClick = { adServices.rewarded.show(activity, RewardType.CHEST_DOUBLE) { onClaim(true) } },
                color = Sun,
                contentColor = Ink,
                leading = { AdTag(stringResource(R.string.ad_label)) },
            )
        }
        DialogButton(
            text = stringResource(R.string.chest_claim, CoinRules.CHEST),
            onClick = { onClaim(false) },
            color = Color.White,
            contentColor = Ink,
        )
    }
}

@Composable
private fun WelcomeDialog(coins: Int, brainy: ShopItem, onDismiss: () -> Unit) {
    GameDialog(onDismissRequest = onDismiss) {
        RibbonBanner(text = stringResource(R.string.welcome_title), color = Sun, tailColor = SunDark, fontSize = 24)
        Box(
            modifier = Modifier
                .size(84.dp)
                .chunky(color = Color.Transparent, shape = RoundedCornerShape(26.dp), depth = 5.dp)
                .brainyBackground(brainy, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrainyFace(Modifier.size(60.dp))
        }
        CoinGainLabel(coins)
        Text(
            text = stringResource(R.string.welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = Lavender,
            textAlign = TextAlign.Center,
        )
        DialogButton(
            text = stringResource(R.string.welcome_confirm),
            onClick = onDismiss,
            color = Sun,
            contentColor = Ink,
        )
    }
}

@Composable
private fun ShieldUsedDialog(use: ShieldUse, onDismiss: () -> Unit, onOpenShop: () -> Unit) {
    GameDialog(onDismissRequest = onDismiss) {
        Box(contentAlignment = Alignment.Center) {
            GameIcon(icon = GameIcons.Shield, size = 96.dp, tint = Ink, fill = Sky, strokeWidth = 2f)
            GameIcon(icon = GameIcons.Flame, size = 40.dp, tint = Ink, fill = Orange, strokeWidth = 1.6f)
        }
        Text(
            text = stringResource(R.string.shield_used_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (use.used == 1) {
                stringResource(R.string.shield_used_desc, use.streakDays)
            } else {
                stringResource(R.string.shield_used_desc_multi, use.used, use.streakDays)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Lavender,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudChip(text = stringResource(R.string.shield_streak_days, use.streakDays)) {
                GameIcon(icon = GameIcons.Flame, size = 18.dp, tint = Orange, fill = Orange, strokeWidth = 1.5f)
            }
            HudChip(text = stringResource(R.string.shield_left, use.shieldsLeft)) {
                GameIcon(icon = GameIcons.Shield, size = 18.dp, tint = Sky, fill = Sky, strokeWidth = 1.5f)
            }
        }
        DialogButton(
            text = stringResource(R.string.shield_start_today),
            onClick = onDismiss,
            color = Sun,
            contentColor = Ink,
        )
        if (use.shieldsLeft == 0) {
            DialogButton(
                text = stringResource(R.string.shield_refill, ShopItem.SHIELD.price),
                onClick = onOpenShop,
                color = NightDeep,
                contentColor = Color.White,
                shadowColor = NightDeeper,
            )
        }
    }
}

@Composable
internal fun ShieldChipIcon() {
    GameIcon(icon = GameIcons.Shield, size = 18.dp, tint = Sky, fill = Sky, strokeWidth = 1.5f)
}

