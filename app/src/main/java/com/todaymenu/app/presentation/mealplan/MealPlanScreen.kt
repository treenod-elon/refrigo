package com.todaymenu.app.presentation.mealplan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.todaymenu.app.presentation.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("식단 관리", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Phase 6에서 구현 예정
            EmptyState(
                icon = Icons.Outlined.CalendarMonth,
                title = "식단이 아직 없어요",
                description = "AI가 보유 재료 기반으로\n주간 식단을 자동 생성해 드릴게요!"
            ) {
                Button(onClick = { /* Phase 6: AI 식단 생성 */ }) {
                    Text("AI 식단 생성하기")
                }
            }
        }
    }
}
