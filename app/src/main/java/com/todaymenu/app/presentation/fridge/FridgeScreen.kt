package com.todaymenu.app.presentation.fridge

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todaymenu.app.presentation.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    onNavigateToScan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("내 냉장고", style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = { /* Phase 2: 음성 입력 */ }) {
                        Icon(Icons.Outlined.Mic, contentDescription = "음성 입력")
                    }
                    IconButton(onClick = { /* Phase 2: 검색 */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "검색")
                    }
                    IconButton(onClick = { /* Phase 2: 수동 추가 */ }) {
                        Icon(Icons.Outlined.Add, contentDescription = "재료 추가")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = "영수증 스캔",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Phase 2에서 구현 예정
            EmptyState(
                icon = Icons.Outlined.Kitchen,
                title = "냉장고가 텅 비었어요!",
                description = "장 보고 오셨나요?"
            ) {
                Button(
                    onClick = onNavigateToScan,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("영수증 스캔하기")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* Phase 2: 직접 추가 */ },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("직접 재료 추가")
                }
            }
        }
    }
}
