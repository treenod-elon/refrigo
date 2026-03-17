package com.todaymenu.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todaymenu.app.presentation.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("오늘 뭐 먹지?", style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "설정"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Phase 5에서 구현 예정
            EmptyState(
                title = "냉장고가 텅 비었어요!",
                description = "장 보고 오셨나요?\n영수증을 스캔하거나 재료를 추가해 보세요."
            ) {
                Button(
                    onClick = onNavigateToScan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("영수증 스캔하기")
                }
            }
        }
    }
}
