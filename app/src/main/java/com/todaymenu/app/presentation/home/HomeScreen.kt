package com.todaymenu.app.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.presentation.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToFridge: () -> Unit = {},
    onNavigateToRecommend: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("오늘 뭐 먹지?", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "설정")
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
                Icon(Icons.Outlined.CameraAlt, contentDescription = "영수증 스캔", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        if (uiState.isEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                EmptyState(
                    title = "냉장고가 텅 비었어요!",
                    description = "장 보고 오셨나요?\n영수증을 스캔하거나 재료를 추가해 보세요."
                ) {
                    Button(
                        onClick = onNavigateToScan,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("영수증 스캔하기")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 유통기한 알림 카드
                if (uiState.expiringIngredients.isNotEmpty()) {
                    ExpiryAlertCard(
                        count = uiState.expiringIngredients.size,
                        names = uiState.expiringIngredients.take(3).joinToString(", ") { it.name },
                        onClick = onNavigateToRecommend,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 냉장고 현황
                FridgeSummarySection(
                    categoryCounts = uiState.categoryCounts,
                    totalCount = uiState.totalIngredientCount,
                    onCategoryClick = { onNavigateToFridge() }
                )

                // 오늘의 추천 메뉴
                TodayRecommendationSection(
                    recommendation = uiState.todayRecommendation,
                    isLoading = uiState.isLoadingRecommendation,
                    onRefresh = { viewModel.refreshRecommendation() },
                    onClickRecommend = onNavigateToRecommend
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ExpiryAlertCard(
    count: Int,
    names: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "유통기한 임박 재료 ${count}개",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "${names}의 유통기한이 곧 만료돼요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "탭하여 이 재료로 메뉴 추천 받기",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FridgeSummarySection(
    categoryCounts: Map<Category, Int>,
    totalCount: Int,
    onCategoryClick: (Category) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("냉장고 현황", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "총 ${totalCount}개",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Category.all) { category ->
                val count = categoryCounts[category] ?: 0
                CategoryCountChip(
                    category = category,
                    count = count,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCountChip(
    category: Category,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (count > 0) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = category.color,
                modifier = Modifier.size(8.dp)
            ) {}
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayRecommendationSection(
    recommendation: com.todaymenu.app.domain.model.MenuRecommendation?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onClickRecommend: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("오늘의 추천 메뉴", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Refresh, contentDescription = "새로고침", modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI가 메뉴를 추천하고 있어요...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            recommendation != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClickRecommend),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = recommendation.menuName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendation.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (recommendation.matchRate > 0) {
                                Text(
                                    "재료 일치율 ${recommendation.matchRate}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (recommendation.cookingTime.isNotEmpty()) {
                                Text(
                                    "조리 ${recommendation.cookingTime}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "탭하여 더 많은 메뉴 추천 받기 →",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            else -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClickRecommend)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "메뉴 추천 받으러 가기",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
