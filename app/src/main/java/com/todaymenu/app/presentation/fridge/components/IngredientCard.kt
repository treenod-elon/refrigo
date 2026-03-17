package com.todaymenu.app.presentation.fridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.presentation.theme.ExpiryDanger
import com.todaymenu.app.presentation.theme.ExpirySafe
import com.todaymenu.app.presentation.theme.ExpiryWarning
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@Composable
fun IngredientCard(
    ingredient: Ingredient,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysUntilExpiry = ingredient.expiryDate?.let {
        ceil((it - System.currentTimeMillis()).toDouble() / 86400000.0).toInt()
    }
    val expiryColor = when {
        daysUntilExpiry == null -> MaterialTheme.colorScheme.onSurfaceVariant
        daysUntilExpiry <= 3 -> ExpiryDanger
        daysUntilExpiry <= 7 -> ExpiryWarning
        else -> ExpirySafe
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 카테고리 컬러 스트라이프
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(ingredient.category.color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // 재료명 + 수량
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${formatQuantity(ingredient.quantity)}${ingredient.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 구매일 + 유통기한
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("M.dd", Locale.KOREA)
                    Text(
                        text = "${dateFormat.format(Date(ingredient.purchaseDate))} 구매",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (daysUntilExpiry != null) {
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // AI 추정 시 ~ 접두사
                        val prefix = if (ingredient.isExpiryEstimated) "~" else ""
                        val expiryText = when {
                            daysUntilExpiry <= 0 -> "${prefix}D+${-daysUntilExpiry}"
                            else -> "${prefix}D-$daysUntilExpiry"
                        }

                        Text(
                            text = expiryText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (daysUntilExpiry <= 3) FontWeight.Bold else FontWeight.Normal,
                            color = expiryColor
                        )

                        // AI 추정 아이콘
                        if (ingredient.isExpiryEstimated) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = "AI가 추정한 유통기한",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 경고 표시
                        if (daysUntilExpiry in 1..3) {
                            Text(
                                text = " ⚠️",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 메모
                if (!ingredient.memo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ingredient.memo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // 편집 아이콘
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 8.dp, end = 4.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "${ingredient.name} 편집",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity == quantity.toLong().toDouble()) {
        quantity.toLong().toString()
    } else {
        quantity.toString()
    }
}
