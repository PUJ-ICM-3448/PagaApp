package com.example.pagaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.pagaapp.ui.screens.home.HomeScreen
import com.example.pagaapp.ui.screens.expenses.ExpensesScreen
import com.example.pagaapp.ui.screens.location.LocationScreen
import com.example.pagaapp.ui.screens.tracking.TrackingScreen
import com.example.pagaapp.ui.screens.history.HistoryScreen
import com.example.pagaapp.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {

        composable(Routes.Home.route) {
            HomeScreen(navController)
        }

        composable(Routes.Expenses.route) {
            ExpensesScreen(navController)
        }

        composable(Routes.Location.route) {
            LocationScreen(navController)
        }

        composable(Routes.Tracking.route) {
            TrackingScreen(navController)
        }

        composable(Routes.History.route) {
            HistoryScreen(navController)
        }

        composable(Routes.Profile.route) {
            ProfileScreen(navController)
        }

    }
}