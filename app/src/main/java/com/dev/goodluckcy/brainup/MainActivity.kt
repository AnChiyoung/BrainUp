package com.dev.goodluckcy.brainup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dev.goodluckcy.brainup.core.designsystem.theme.BrainUpTheme
import com.dev.goodluckcy.brainup.ui.BrainUpApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainUpTheme {
                BrainUpApp()
            }
        }
    }
}
