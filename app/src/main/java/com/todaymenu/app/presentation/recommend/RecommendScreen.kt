package com.todaymenu.app.presentation.recommend

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.todaymenu.app.presentation.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("메뉴 추천", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Phase 4에서 구현 예정
            EmptyState(
                icon = Icons.Outlined.Restaurant,
                title = "추천할 메뉴가 없어요",
                description = "먼저 냉장고에 재료를 추가해 주세요.\nAI가 맛있는 메뉴를 추천해 드릴게요!"
            )
        }
    }
}
