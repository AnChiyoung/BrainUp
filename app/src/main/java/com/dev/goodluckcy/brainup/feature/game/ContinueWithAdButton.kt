package com.dev.goodluckcy.brainup.feature.game

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.analytics.RewardType

/**
 * 오답 후 '광고 보고 이어하기' 버튼. 보상형 광고가 준비된 경우에만 보인다.
 * 사용자가 보상을 받고 광고를 닫으면 [onContinue]가 호출된다.
 */
@Composable
fun ContinueWithAdButton(
    canContinue: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val adServices = LocalAdServices.current ?: return
    val activity = LocalActivity.current ?: return
    val isLoaded by adServices.rewarded.isLoaded.collectAsStateWithLifecycle()
    if (!canContinue || !isLoaded) return

    OutlinedButton(
        onClick = { adServices.rewarded.show(activity, RewardType.CONTINUE, onContinue) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text(
            text = stringResource(R.string.action_continue_with_ad),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
