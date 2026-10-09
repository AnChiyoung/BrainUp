package com.dev.goodluckcy.brainup.core.designsystem.component

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 화면 위에 떠 있는 하단 탭 바의 높이(시스템 내비게이션 바 제외) */
val TabBarSpace = 86.dp

/** 탭 화면 콘텐츠가 하단 탭 바에 가리지 않도록 아래 여백을 둔다. */
fun Modifier.tabBarPadding(): Modifier = this
    .navigationBarsPadding()
    .padding(bottom = TabBarSpace)
