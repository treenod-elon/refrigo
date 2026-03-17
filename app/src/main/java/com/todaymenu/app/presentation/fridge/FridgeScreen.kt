package com.todaymenu.app.presentation.fridge

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaymenu.app.domain.model.StorageType
import com.todaymenu.app.presentation.common.components.EmptyState
import com.todaymenu.app.presentation.common.components.LoadingIndicator
import com.todaymenu.app.presentation.common.components.VoiceInputDialog
import com.todaymenu.app.presentation.fridge.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    onNavigateToScan: () -> Unit,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Snackbar
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.isSearchActive) {
                // 검색 모드 앱바
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    leadingIcon = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "검색 닫기")
                        }
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Outlined.Close, contentDescription = "지우기")
                            }
                        }
                    },
                    placeholder = { Text("재료 검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {}
            } else {
                TopAppBar(
                    title = { Text("내 냉장고", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { showVoiceDialog = true }) {
                            Icon(Icons.Outlined.Mic, contentDescription = "음성 입력")
                        }
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Outlined.Search, contentDescription = "검색")
                        }
                        IconButton(onClick = { viewModel.showAddDialog() }) {
                            Icon(Icons.Outlined.Add, contentDescription = "재료 추가")
                        }
                    }
                )
            }
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
            // 보관위치 탭
            StorageFilterTabs(
                selectedStorage = uiState.selectedStorageType,
                onStorageSelected = { viewModel.setStorageFilter(it) }
            )

            // 정렬 드롭다운
            SortDropdown(
                currentSort = uiState.sortType,
                onSortSelected = { viewModel.setSortType(it) }
            )

            // 재료 목록
            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.filteredIngredients.isEmpty()) {
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
                        onClick = { showVoiceDialog = true },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("음성으로 추가")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.showAddDialog() },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("직접 재료 추가")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    uiState.filteredIngredients.forEach { (category, ingredients) ->
                        item(key = "header_${category.name}") {
                            CategorySection(
                                category = category,
                                ingredients = ingredients,
                                isExpanded = uiState.expandedCategories.contains(category),
                                onToggle = { viewModel.toggleCategory(category) },
                                onEditIngredient = { viewModel.showEditSheet(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 재료 추가 다이얼로그
    if (uiState.showAddDialog) {
        AddIngredientDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { viewModel.addIngredient(it) }
        )
    }

    // 재료 편집 바텀시트
    uiState.editingIngredient?.let { ingredient ->
        IngredientEditSheet(
            ingredient = ingredient,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { viewModel.updateIngredient(it) },
            onDelete = {
                viewModel.deleteIngredient(it)
                viewModel.hideEditSheet()
            }
        )
    }

    // 음성 입력 다이얼로그
    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onResult = { recognizedText ->
                showVoiceDialog = false
                viewModel.processVoiceInput(recognizedText)
            }
        )
    }

    // 음성 인식 결과 확인 다이얼로그
    if (uiState.isVoiceProcessing || uiState.voiceParsedIngredients.isNotEmpty()) {
        VoiceResultConfirmDialog(
            ingredients = uiState.voiceParsedIngredients,
            isLoading = uiState.isVoiceProcessing,
            onConfirm = { selected ->
                viewModel.confirmVoiceIngredients(selected)
            },
            onDismiss = { viewModel.dismissVoiceResult() }
        )
    }
}

@Composable
private fun StorageFilterTabs(
    selectedStorage: StorageType?,
    onStorageSelected: (StorageType?) -> Unit
) {
    val tabs = listOf<Pair<StorageType?, String>>(
        null to "전체",
        StorageType.FRIDGE to "냉장",
        StorageType.FREEZER to "냉동",
        StorageType.ROOM to "실온"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (storage, label) ->
            FilterChip(
                selected = selectedStorage == storage,
                onClick = { onStorageSelected(storage) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun SortDropdown(
    currentSort: SortType,
    onSortSelected: (SortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    "정렬: ${currentSort.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortType.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = { Text(sort.label) },
                        onClick = {
                            onSortSelected(sort)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
