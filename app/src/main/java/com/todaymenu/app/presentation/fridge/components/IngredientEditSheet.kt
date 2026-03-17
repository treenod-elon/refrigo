package com.todaymenu.app.presentation.fridge.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun IngredientEditSheet(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit,
    onDelete: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(ingredient.name) }
    var selectedCategory by remember { mutableStateOf(ingredient.category) }
    var quantity by remember { mutableStateOf(ingredient.quantity.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }) }
    var selectedUnit by remember { mutableStateOf(ingredient.unit) }
    var expiryDate by remember { mutableStateOf(ingredient.expiryDate) }
    var isExpiryEstimated by remember { mutableStateOf(ingredient.isExpiryEstimated) }
    var selectedStorage by remember { mutableStateOf(ingredient.storageType) }
    var memo by remember { mutableStateOf(ingredient.memo ?: "") }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val units = listOf("개", "g", "kg", "ml", "L", "팩", "단", "모", "봉", "마리", "근")
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "재료 편집",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 재료명
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("재료명") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 카테고리
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
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

            // 유통기한
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("유통기한", style = MaterialTheme.typography.labelLarge)
                    if (isExpiryEstimated) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "AI가 추정한 날짜입니다",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = expiryDate?.let { dateFormat.format(Date(it)) } ?: "미설정",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    TextButton(onClick = { showExpiryDatePicker = true }) {
                        Text("날짜 변경")
                    }
                    if (expiryDate != null) {
                        TextButton(onClick = {
                            expiryDate = null
                            isExpiryEstimated = false
                        }) {
                            Text("초기화")
                        }
                    }
                }
            }

            // 보관위치
            Text("보관위치", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            // 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("삭제")
                }

                Button(
                    onClick = {
                        onSave(
                            ingredient.copy(
                                name = name.trim(),
                                category = selectedCategory,
                                quantity = quantity.toDoubleOrNull() ?: 1.0,
                                unit = selectedUnit,
                                expiryDate = expiryDate,
                                isExpiryEstimated = if (expiryDate != ingredient.expiryDate) false else isExpiryEstimated,
                                storageType = selectedStorage,
                                memo = memo.ifBlank { null }
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("저장")
                }
            }
        }
    }

    // 유통기한 DatePicker
    if (showExpiryDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate ?: (System.currentTimeMillis() + 7 * 86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expiryDate = it
                        isExpiryEstimated = false
                    }
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

    // 삭제 확인
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("재료 삭제") },
            text = { Text("${ingredient.name}을(를) 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(ingredient)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    }
}
