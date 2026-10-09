package com.dev.goodluckcy.brainup.feature.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.designsystem.component.GamePanel
import com.dev.goodluckcy.brainup.core.designsystem.component.nightSky
import com.dev.goodluckcy.brainup.core.designsystem.component.tabBarPadding
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.core.designsystem.theme.Lavender
import com.dev.goodluckcy.brainup.core.designsystem.theme.NightLight
import com.dev.goodluckcy.brainup.core.designsystem.theme.Sun

// TODO: 소리/진동(DataStore), 개인정보처리방침 링크(URL 확정 후)
@Composable
fun SettingsScreen() {
    val adServices = LocalAdServices.current
    val activity = LocalActivity.current
    val isPrivacyOptionsRequired =
        adServices?.consent?.isPrivacyOptionsRequired?.collectAsStateWithLifecycle()?.value ?: false
    SettingsContent(
        isPrivacyOptionsRequired = isPrivacyOptionsRequired,
        onPrivacyOptionsClick = {
            if (adServices != null && activity != null) adServices.consent.showPrivacyOptionsForm(activity)
        },
        versionName = BuildConfig.VERSION_NAME,
    )
}

@Composable
private fun SettingsContent(
    isPrivacyOptionsRequired: Boolean,
    onPrivacyOptionsClick: () -> Unit,
    versionName: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky(variant = 2)
            .statusBarsPadding()
            .tabBarPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        SectionHeader(stringResource(R.string.settings_section_ads))
        GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isPrivacyOptionsRequired) {
                            Modifier.clickable(role = Role.Button, onClick = onPrivacyOptionsClick)
                        } else {
                            Modifier
                        },
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.settings_ad_privacy), style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    text = stringResource(
                        if (isPrivacyOptionsRequired) R.string.settings_ad_privacy_desc else R.string.settings_ad_privacy_not_required,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Lavender,
                )
            }
        }
        SectionHeader(stringResource(R.string.settings_section_app))
        GamePanel(modifier = Modifier.fillMaxWidth(), depth = 5.dp) {
            Column {
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { uriHandler.openUri(PRIVACY_POLICY_URL) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_privacy_policy),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text("›", style = MaterialTheme.typography.titleLarge, color = Lavender)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(NightLight),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(versionName, style = MaterialTheme.typography.bodyLarge, color = Lavender)
                }
            }
        }
    }
}

/** GitHub Pages에 게시한 개인정보처리방침 (원본: AnChiyoung/BrainUp-privacy) */
private const val PRIVACY_POLICY_URL = "https://anchiyoung.github.io/BrainUp-privacy/"

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        color = Sun,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsContentPreview() {
    BrainUpTheme {
        SettingsContent(
            isPrivacyOptionsRequired = true,
            onPrivacyOptionsClick = {},
            versionName = "1.0.0",
        )
    }
}
