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
import com.example.pagaapp.ui.screens.history.AddTransactionScreen
import com.example.pagaapp.ui.screens.history.HistoryScreen
import com.example.pagaapp.ui.screens.home.HomeScreen
import com.example.pagaapp.ui.screens.location.DeliveryScreen
import com.example.pagaapp.ui.screens.login.LoginScreen
import com.example.pagaapp.ui.screens.login.RegisterScreen
import com.example.pagaapp.ui.screens.profile.ProfileScreen
import com.example.pagaapp.ui.screens.tracking.TrackingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authRoutes = listOf(Routes.Login.route, Routes.Register.route)

    Scaffold(
        bottomBar = {
            if (currentRoute !in authRoutes) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Login.route) { LoginScreen(navController) }
            composable(Routes.Register.route) { RegisterScreen(navController) }
            composable(Routes.Home.route) { HomeScreen(navController) }
            composable(Routes.Expenses.route) { ExpensesScreen(navController) }
            composable(
                route = Routes.RegisterPayment.route,
                arguments = listOf(navArgument("debtId") { type = NavType.StringType })
            ) { backStackEntry ->
                val debtId = backStackEntry.arguments?.getString("debtId")
                RegisterPaymentScreen(navController, debtId)
            }
            composable(Routes.History.route) { HistoryScreen(navController) }
            
            composable(
                route = Routes.AddTransaction.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "expense"
                AddTransactionScreen(initialType = type, onBack = { navController.popBackStack() })
            }

            composable(Routes.Delivery.route) {
                DeliveryScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.Location.route) { 
                TrackingScreen(onBack = { navController.popBackStack() }, showBack = true) 
            }
            composable(Routes.Tracking.route) { 
                TrackingScreen(onBack = { navController.popBackStack() }, showBack = true) 
            }
            
            composable(Routes.Profile.route) { ProfileScreen(navController) }
        }
    }
}
