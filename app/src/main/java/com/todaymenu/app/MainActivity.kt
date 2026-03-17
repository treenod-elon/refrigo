package com.todaymenu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.todaymenu.app.data.local.datastore.UserPreferences
import com.todaymenu.app.presentation.common.components.BottomNavBar
import com.todaymenu.app.presentation.navigation.NavGraph
import com.todaymenu.app.presentation.navigation.Screen
import com.todaymenu.app.presentation.theme.TodayMenuTheme
import com.todaymenu.app.data.worker.ExpiryNotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ExpiryNotificationWorker.schedule(this)
        setContent {
            TodayMenuTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val prefsState by userPreferences.userPreferences.collectAsState(initial = null)
                val startDestination = remember(prefsState) {
                    when {
                        prefsState == null -> null // 아직 로딩 중
                        prefsState!!.isFirstLaunch -> Screen.Onboarding.route
                        else -> Screen.Home.route
                    }
                }

                // 바텀 네비게이션이 보이는 화면 (메인 탭 4개)
                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Fridge.route,
                    Screen.Recommend.route,
                    Screen.MealPlan.route
                )

                if (startDestination != null) {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(navController)
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
