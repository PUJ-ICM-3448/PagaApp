package com.example.pagaapp.navigation

sealed class Routes(val route: String) {

    object Login : Routes("login")
    object Home : Routes("home")
    object Expenses : Routes("expenses")
    object Location : Routes("location")
    object Tracking : Routes("tracking")
    object History : Routes("history")
    object Profile : Routes("profile")
    object RegisterPayment : Routes("register_payment/{debtId}") {
        fun createRoute(debtId: String) = "register_payment/$debtId"
    }

}
