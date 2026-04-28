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
import com.example.pagaapp.ui.screens.login.RegisterScreen
import com.example.pagaapp.ui.screens.profile.ProfileScreen
import com.example.pagaapp.ui.screens.tracking.TrackingScreen
import com.example.pagaapp.ui.screens.settings.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authRoutes = listOf(Routes.Login.route, Routes.Register.route)
    val settingsRoutes = listOf(
        Routes.PaymentMethods.route,
        Routes.Security.route,
        Routes.HelpSupport.route,
        Routes.AppSettings.route
    )

    Scaffold(
        bottomBar = {
            // No mostrar barra si es login, registro o sub-pantallas de configuración (opcional)
            if (currentRoute !in authRoutes && currentRoute !in settingsRoutes) {
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
            composable(Routes.Location.route) { LocationScreen(navController) }
            composable(Routes.Tracking.route) { TrackingScreen() }
            composable(Routes.Profile.route) { ProfileScreen(navController) }

            // Rutas de Configuración
            composable(Routes.PaymentMethods.route) { PaymentMethodsScreen(navController) }
            composable(Routes.Security.route) { SecurityScreen(navController) }
            composable(Routes.HelpSupport.route) { HelpSupportScreen(navController) }
            composable(Routes.AppSettings.route) { AppSettingsScreen(navController) }
        }
    }
}
