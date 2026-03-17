package com.todaymenu.app.presentation.fridge.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.StorageType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onConfirm: (Ingredient) -> Unit,
    existingNames: List<String> = emptyList()
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.ETC) }
    var quantity by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("개") }
    var purchaseDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var expiryDate by remember { mutableStateOf<Long?>(null) }
    var selectedStorage by remember { mutableStateOf(StorageType.FRIDGE) }
    var memo by remember { mutableStateOf("") }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }

    // 자동완성 필터
    val suggestions = remember(name) {
        if (name.length >= 1) existingNames.filter { it.contains(name, ignoreCase = true) }.take(5)
        else emptyList()
    }
    var showSuggestions by remember { mutableStateOf(false) }

    val units = listOf("개", "g", "kg", "ml", "L", "팩", "단", "모", "봉", "마리", "근")
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("재료 추가", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 재료명 (필수)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showSuggestions = it.isNotEmpty() && suggestions.isNotEmpty()
                    },
                    label = { Text("재료명 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 자동완성 제안
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Column {
                        suggestions.forEach { suggestion ->
                            TextButton(
                                onClick = {
                                    name = suggestion
                                    showSuggestions = false
                                }
                            ) {
                                Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // 카테고리 (필수)
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("카테고리 *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        Category.all.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.label) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // 수량 + 단위
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("수량") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("단위") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 구매일 (필수, 기본: 오늘)
                OutlinedTextField(
                    value = dateFormat.format(Date(purchaseDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("구매일 *") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = true
                )
                TextButton(onClick = { showPurchaseDatePicker = true }) {
                    Text("구매일 변경")
                }

                // 유통기한 (선택)
                OutlinedTextField(
                    value = expiryDate?.let { dateFormat.format(Date(it)) } ?: "미입력 (AI 자동 추정)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("유통기한") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    TextButton(onClick = { showExpiryDatePicker = true }) {
                        Text("유통기한 설정")
                    }
                    if (expiryDate != null) {
                        TextButton(onClick = { expiryDate = null }) {
                            Text("초기화")
                        }
                    }
                }

                // 보관위치
                Text("보관위치", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StorageType.entries.forEach { storage ->
                        FilterChip(
                            selected = selectedStorage == storage,
                            onClick = { selectedStorage = storage },
                            label = { Text(storage.label) }
                        )
                    }
                }

                // 메모
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Ingredient(
                            name = name.trim(),
                            category = selectedCategory,
                            quantity = quantity.toDoubleOrNull() ?: 1.0,
                            unit = selectedUnit,
                            purchaseDate = purchaseDate,
                            expiryDate = expiryDate,
                            storageType = selectedStorage,
                            memo = memo.ifBlank { null }
                        )
                    )
                },
                enabled = isValid
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )

    // DatePicker dialogs
    if (showPurchaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseDate
        )
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { purchaseDate = it }
                    showPurchaseDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showExpiryDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate ?: (System.currentTimeMillis() + 7 * 86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { expiryDate = it }
                    showExpiryDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
