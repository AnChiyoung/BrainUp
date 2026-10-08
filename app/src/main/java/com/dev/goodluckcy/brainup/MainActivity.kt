package com.dev.goodluckcy.brainup

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.dev.goodluckcy.brainup.core.ads.AdServices
import com.dev.goodluckcy.brainup.core.ads.LocalAdServices
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.ui.BrainUpApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adServices: AdServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 배경이 항상 어두우므로 시스템 바 아이콘을 밝게 고정한다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        adServices.consent.gatherConsent(this)
        setContent {
            BrainUpTheme {
                CompositionLocalProvider(LocalAdServices provides adServices) {
                    BrainUpApp()
                }
            }
        }
    }
}
