package com.todaymenu.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.todaymenu.app.presentation.fridge.FridgeScreen
import com.todaymenu.app.presentation.home.HomeScreen
import com.todaymenu.app.presentation.mealplan.MealPlanScreen
import com.todaymenu.app.presentation.recommend.RecommendScreen
import com.todaymenu.app.presentation.scan.ScanScreen
import com.todaymenu.app.presentation.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToScan = { navController.navigate(Screen.Scan.route) }
            )
        }

        composable(Screen.Fridge.route) {
            FridgeScreen(
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
            )
        }

        composable(Screen.Recommend.route) {
            RecommendScreen()
        }

        composable(Screen.MealPlan.route) {
            MealPlanScreen()
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
