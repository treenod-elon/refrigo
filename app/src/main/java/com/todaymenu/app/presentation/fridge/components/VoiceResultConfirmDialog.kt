package com.todaymenu.app.presentation.fridge.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todaymenu.app.domain.usecase.ScannedIngredient
import com.todaymenu.app.presentation.common.components.LoadingIndicator

@Composable
fun VoiceResultConfirmDialog(
    ingredients: List<ScannedIngredient>,
    isLoading: Boolean,
    onConfirm: (List<ScannedIngredient>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedStates = remember(ingredients) {
        mutableStateListOf(*ingredients.map { it.isSelected }.toTypedArray())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("음성 인식 결과", style = MaterialTheme.typography.titleLarge) },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "음성을 분석하고 있어요...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "추가할 재료를 확인해 주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    ingredients.forEachIndexed { index, ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedStates[index],
                                onCheckedChange = { selectedStates[index] = it }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${ingredient.quantity} ${ingredient.unit} · ${ingredient.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                Button(
                    onClick = {
                        val selected = ingredients.filterIndexed { index, _ -> selectedStates[index] }
                        onConfirm(selected)
                    },
                    enabled = selectedStates.any { it }
                ) {
                    Text("추가 (${selectedStates.count { it }}개)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
