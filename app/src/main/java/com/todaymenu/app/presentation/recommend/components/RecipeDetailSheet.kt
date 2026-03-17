package com.todaymenu.app.presentation.recommend.components

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.todaymenu.app.domain.model.MenuRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailSheet(
    menu: MenuRecommendation,
    onDismiss: () -> Unit,
    onCompleteCooking: () -> Unit
) {
    val context = LocalContext.current

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
            // 제목
            Text(
                text = menu.menuName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // 메타 정보
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (menu.cookingTime.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(menu.cookingTime) },
                        leadingIcon = { Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                if (menu.difficulty.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(menu.difficulty) },
                        leadingIcon = { Icon(Icons.Outlined.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                if (menu.servings.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(menu.servings) },
                        leadingIcon = { Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // 재료
            Text("재료", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                menu.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (ingredient.isAvailable) Icons.Outlined.CheckCircle else Icons.Outlined.HelpOutline,
                            contentDescription = if (ingredient.isAvailable) "보유" else "미확인",
                            modifier = Modifier.size(18.dp),
                            tint = if (ingredient.isAvailable) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${ingredient.name} ${ingredient.amount}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (ingredient.isAvailable) "보유" else "미확인",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ingredient.isAvailable) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 기본 양념
            if (menu.seasonings.isNotEmpty()) {
                Text("기본 양념", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = menu.seasonings.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 조리 순서
            if (menu.steps.isNotEmpty()) {
                Text("조리 순서", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    menu.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 팁
            if (menu.tip.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = menu.tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            HorizontalDivider()

            // 외부 링크
            if (menu.youtubeSearchUrl.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        try {
                            CustomTabsIntent.Builder().build()
                                .launchUrl(context, Uri.parse(menu.youtubeSearchUrl))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.OndemandVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("유튜브에서 레시피 보기")
                }
            }

            if (menu.blogSearchUrl.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        try {
                            CustomTabsIntent.Builder().build()
                                .launchUrl(context, Uri.parse(menu.blogSearchUrl))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Article, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("블로그에서 레시피 보기")
                }
            }

            // 요리 완료 버튼
            Button(
                onClick = onCompleteCooking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("요리 완료 — 재료 차감")
            }
        }
    }
}
