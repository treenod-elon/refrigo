package com.todaymenu.app.presentation.recommend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaymenu.app.presentation.common.components.EmptyState
import com.todaymenu.app.presentation.recommend.components.MenuCard
import com.todaymenu.app.presentation.recommend.components.RecipeDetailSheet

private val cuisineTypes = listOf("빠른요리", "한식", "중식", "양식")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("메뉴 추천", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        if (uiState.availableIngredients.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                EmptyState(
                    icon = Icons.Outlined.Restaurant,
                    title = "추천할 메뉴가 없어요",
                    description = "먼저 냉장고에 재료를 추가해 주세요.\nAI가 맛있는 메뉴를 추천해 드릴게요!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 재료 선택
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("사용할 재료 선택", style = MaterialTheme.typography.titleSmall)
                            Row {
                                TextButton(onClick = { viewModel.selectAllIngredients() }) {
                                    Text("전체", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { viewModel.deselectAllIngredients() }) {
                                    Text("해제", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableIngredients) { ingredient ->
                            FilterChip(
                                selected = uiState.selectedIngredientNames.contains(ingredient.name),
                                onClick = { viewModel.toggleIngredient(ingredient.name) },
                                label = { Text(ingredient.name) }
                            )
                        }
                    }
                }

                // 유형 탭
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cuisineTypes.forEach { type ->
                            FilterChip(
                                selected = uiState.cuisineType == type,
                                onClick = { viewModel.setCuisineType(type) },
                                label = { Text(type) }
                            )
                        }
                    }
                }

                // 추천 받기 버튼
                item {
                    Button(
                        onClick = { viewModel.getRecommendations() },
                        enabled = !uiState.isLoading && uiState.selectedIngredientNames.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("메뉴를 찾고 있어요...")
                        } else {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("추천 받기")
                        }
                    }
                }

                // 추천 결과
                if (uiState.recommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "${uiState.cuisineType} 추천 (${uiState.recommendations.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    itemsIndexed(uiState.recommendations) { index, menu ->
                        MenuCard(
                            menu = menu,
                            index = index,
                            onShowRecipe = { viewModel.showRecipeDetail(menu) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // 더보기 버튼
                    if (uiState.canLoadMore) {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.loadMore() },
                                enabled = !uiState.isLoadingMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                if (uiState.isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("메뉴를 더 찾고 있어요...")
                                } else {
                                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("더보기 (3개 더)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 레시피 상세 바텀시트
    uiState.selectedRecipe?.let { recipe ->
        RecipeDetailSheet(
            menu = recipe,
            onDismiss = { viewModel.hideRecipeDetail() },
            onCompleteCooking = { viewModel.completeCooking(recipe) }
        )
    }
}
