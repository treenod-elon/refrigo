package com.todaymenu.app.presentation.mealplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaymenu.app.domain.model.MealPlan
import com.todaymenu.app.domain.model.MealType
import com.todaymenu.app.domain.model.ShoppingItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("식단 관리", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 주간 네비게이션
            item {
                WeekNavigator(
                    weekStart = uiState.weekStart,
                    onPrevious = { viewModel.navigateWeek(false) },
                    onNext = { viewModel.navigateWeek(true) }
                )
            }

            // 주간 식단
            if (uiState.mealPlans.isEmpty() && !uiState.isGenerating) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("이번 주 식단이 없어요", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "AI가 보유 재료 기반으로 식단을 자동 생성해요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // 날짜별 그룹
                val grouped = uiState.mealPlans.groupBy { it.date }
                    .toSortedMap()
                val dateFormat = SimpleDateFormat("M/d (E)", Locale.KOREA)

                grouped.forEach { (date, meals) ->
                    item(key = "day_$date") {
                        DayMealCard(
                            dateLabel = dateFormat.format(Date(date)),
                            meals = meals,
                            onDelete = { viewModel.deleteMealPlan(it) }
                        )
                    }
                }
            }

            // AI 식단 생성 버튼
            item {
                if (uiState.isGenerating) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(uiState.generatingMessage, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.toggleGenerateOptions() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 식단 생성하기")
                    }
                }
            }

            // 생성 옵션
            if (uiState.showGenerateOptions) {
                item {
                    GenerateOptionsCard(
                        durationDays = uiState.durationDays,
                        selectedMeals = uiState.selectedMeals,
                        preference = uiState.preference,
                        onDurationChange = { viewModel.setDuration(it) },
                        onMealToggle = { viewModel.toggleMeal(it) },
                        onPreferenceChange = { viewModel.setPreference(it) },
                        onGenerate = { viewModel.generateMealPlan() }
                    )
                }
            }

            // 영양 노트
            if (uiState.nutritionNote.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.nutritionNote, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // 장보기 목록
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("장보기 목록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (uiState.shoppingItems.any { !it.isPurchased }) {
                        TextButton(onClick = { viewModel.shareShoppingList(context) }) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("공유")
                        }
                    }
                }
            }

            items(uiState.shoppingItems, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { viewModel.toggleShoppingItem(item) },
                    onDelete = { viewModel.deleteShoppingItem(item) }
                )
            }

            // 직접 추가
            item {
                AddShoppingItemRow(onAdd = { name, amount -> viewModel.addShoppingItem(name, amount) })
            }

            // 에러 메시지
            uiState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekNavigator(weekStart: Long, onPrevious: () -> Unit, onNext: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM.dd", Locale.KOREA)
    val startStr = dateFormat.format(Date(weekStart))
    val endStr = dateFormat.format(Date(weekStart + 6 * 86400000L))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "이전 주")
        }
        Text(
            text = "$startStr ~ $endStr",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "다음 주")
        }
    }
}

@Composable
private fun DayMealCard(dateLabel: String, meals: List<MealPlan>, onDelete: (MealPlan) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(dateLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            MealType.entries.forEach { mealType ->
                val meal = meals.find { it.mealType == mealType }
                if (meal != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mealType.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = meal.menuName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDelete(meal) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "${meal.menuName} 삭제",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateOptionsCard(
    durationDays: Int,
    selectedMeals: List<String>,
    preference: String,
    onDurationChange: (Int) -> Unit,
    onMealToggle: (String) -> Unit,
    onPreferenceChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("생성 옵션", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Text("기간", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7 to "1주일", 14 to "2주일").forEach { (days, label) ->
                    FilterChip(
                        selected = durationDays == days,
                        onClick = { onDurationChange(days) },
                        label = { Text(label) }
                    )
                }
            }

            Text("끼니", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("아침", "점심", "저녁").forEach { meal ->
                    FilterChip(
                        selected = selectedMeals.contains(meal),
                        onClick = { onMealToggle(meal) },
                        label = { Text(meal) }
                    )
                }
            }

            Text("선호", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("한식위주", "다양하게").forEach { pref ->
                    FilterChip(
                        selected = preference == pref,
                        onClick = { onPreferenceChange(pref) },
                        label = { Text(pref) }
                    )
                }
            }

            Button(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("생성하기")
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isPurchased,
            onCheckedChange = { onToggle() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None
            )
            item.amount?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "${item.name} 삭제",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddShoppingItemRow(onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("재료명") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            placeholder = { Text("수량") },
            singleLine = true,
            modifier = Modifier.weight(0.5f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (name.isNotBlank()) {
                        onAdd(name, amount)
                        name = ""
                        amount = ""
                    }
                }
            )
        )
        IconButton(
            onClick = {
                if (name.isNotBlank()) {
                    onAdd(name, amount)
                    name = ""
                    amount = ""
                }
            }
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "추가")
        }
    }
}
