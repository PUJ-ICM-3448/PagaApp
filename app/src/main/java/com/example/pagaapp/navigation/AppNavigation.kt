package com.example.pagaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pagaapp.ui.screens.expenses.ExpensesScreen
import com.example.pagaapp.ui.screens.expenses.RegisterPaymentScreen
import com.example.pagaapp.ui.screens.history.HistoryScreen
import com.example.pagaapp.ui.screens.home.HomeScreen
import com.example.pagaapp.ui.screens.location.LocationScreen
import com.example.pagaapp.ui.screens.login.LoginScreen
import com.example.pagaapp.ui.screens.profile.ProfileScreen
import com.example.pagaapp.ui.screens.profile.SensorsScreen
import com.example.pagaapp.ui.screens.tracking.TrackingScreen
import com.example.pagaapp.ui.screens.repartidor.RepartidorScreen
import com.example.pagaapp.ui.screens.cash.RequestCashScreen
import com.example.pagaapp.ui.screens.cash.TrackingRequestScreen
import com.example.pagaapp.ui.screens.repartidor.DeliveryMapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val hideBottomBar = listOf(
                Routes.Login.route,
                Routes.Repartidor.route,
                Routes.DeliveryMap.route,
                Routes.TrackingRequest.route
            )
            val shouldShowBottomBar = currentRoute !in hideBottomBar && 
                                     currentRoute?.startsWith("tracking_request") == false && 
                                     currentRoute?.startsWith("delivery_map") == false
            
            if (shouldShowBottomBar) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Login.route) {
                LoginScreen(navController)
            }

            composable(Routes.Home.route) {
                HomeScreen(navController)
            }

            composable(Routes.Expenses.route) {
                ExpensesScreen(navController)
            }

            composable(
                route = Routes.RegisterPayment.route,
                arguments = listOf(navArgument("debtId") { type = NavType.StringType })
            ) { backStackEntry ->
                val debtId = backStackEntry.arguments?.getString("debtId")
                RegisterPaymentScreen(navController, debtId)
            }

            composable(Routes.History.route) {
                HistoryScreen(navController)
            }

            composable(Routes.Location.route) {
                LocationScreen(navController)
            }

            composable(Routes.RequestCash.route) {
                RequestCashScreen(navController)
            }

            composable(
                route = Routes.TrackingRequest.route,
                arguments = listOf(navArgument("solicitudId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("solicitudId") ?: ""
                TrackingRequestScreen(id, navController)
            }

            composable(Routes.Tracking.route) {
                TrackingScreen()
            }

            composable(Routes.Profile.route) {
                ProfileScreen(navController)
            }

            composable(Routes.Sensors.route) {
                SensorsScreen(navController)
            }

            composable(Routes.Repartidor.route) {
                RepartidorScreen(navController)
            }

            composable(
                route = Routes.DeliveryMap.route,
                arguments = listOf(navArgument("solicitudId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("solicitudId") ?: ""
                DeliveryMapScreen(id, navController)
            }
        }
    }
}
