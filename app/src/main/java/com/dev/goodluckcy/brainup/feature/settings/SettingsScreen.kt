package com.dev.goodluckcy.brainup.feature.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.goodluckcy.brainup.BuildConfig
import com.dev.goodluckcy.brainup.R
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    isPrivacyOptionsRequired: Boolean,
    onPrivacyOptionsClick: () -> Unit,
    versionName: String,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_settings), fontWeight = FontWeight.Bold) })
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SectionHeader(stringResource(R.string.settings_section_ads))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_ad_privacy)) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (isPrivacyOptionsRequired) {
                                R.string.settings_ad_privacy_desc
                            } else {
                                R.string.settings_ad_privacy_not_required
                            },
                        ),
                    )
                },
                modifier = if (isPrivacyOptionsRequired) {
                    Modifier.clickable(onClick = onPrivacyOptionsClick)
                } else {
                    Modifier
                },
            )
            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_section_app))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_version)) },
                trailingContent = { Text(versionName) },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    BrainUpTheme {
        SettingsContent(
            isPrivacyOptionsRequired = true,
            onPrivacyOptionsClick = {},
            versionName = "1.0",
        )
    }
}
