package com.todaymenu.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // 버튼
    small = RoundedCornerShape(12.dp),
    // 카드
    medium = RoundedCornerShape(16.dp),
    // 다이얼로그
    large = RoundedCornerShape(24.dp),
    // 바텀시트 상단
    extraLarge = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
)
