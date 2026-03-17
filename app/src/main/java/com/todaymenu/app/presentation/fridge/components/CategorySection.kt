package com.todaymenu.app.presentation.fridge.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient

@Composable
fun CategorySection(
    category: Category,
    ingredients: List<Ingredient>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEditIngredient: (Ingredient) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 카테고리 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카테고리 이름 + 색상 도트
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(end = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = category.color,
                    modifier = Modifier.size(8.dp)
                ) {}
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = category.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${ingredients.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp
                else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "${category.label} 접기" else "${category.label} 펼치기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 재료 카드 목록
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredients.forEach { ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        onEditClick = { onEditIngredient(ingredient) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
