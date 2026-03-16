package com.example.pagaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pagaapp.ui.screens.expenses.ExpensesScreen
import com.example.pagaapp.ui.screens.history.HistoryScreen
import com.example.pagaapp.ui.screens.home.HomeScreen
import com.example.pagaapp.ui.screens.location.LocationScreen
import com.example.pagaapp.ui.screens.profile.ProfileScreen
import com.example.pagaapp.ui.screens.tracking.TrackingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(navController)
            }

            composable(Routes.Expenses.route) {
                ExpensesScreen(navController)
            }

            composable(Routes.History.route) {
                HistoryScreen(navController)
            }

            composable(Routes.Location.route) {
                LocationScreen(navController)
            }

            composable(Routes.Tracking.route) {
                TrackingScreen(navController)
            }

            composable(Routes.Profile.route) {
                ProfileScreen(navController)
            }
        }
    }
}
