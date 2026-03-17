package com.todaymenu.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Fridge : Screen("fridge")
    data object Recommend : Screen("recommend")
    data object MealPlan : Screen("meal_plan")
    data object Scan : Screen("scan")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "홈",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Fridge,
        label = "냉장고",
        selectedIcon = Icons.Rounded.Kitchen,
        unselectedIcon = Icons.Outlined.Kitchen
    ),
    BottomNavItem(
        screen = Screen.Recommend,
        label = "추천",
        selectedIcon = Icons.Rounded.Restaurant,
        unselectedIcon = Icons.Outlined.Restaurant
    ),
    BottomNavItem(
        screen = Screen.MealPlan,
        label = "식단",
        selectedIcon = Icons.Rounded.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
)
