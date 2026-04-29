package com.example.pagaapp.navigation

sealed class Routes(val route: String) {

    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object Expenses : Routes("expenses")
    object Location : Routes("location")
    object Tracking : Routes("tracking")
    object History : Routes("history")
    object Profile : Routes("profile")
    object RegisterPayment : Routes("register_payment/{debtId}") {
        fun createRoute(debtId: String) = "register_payment/$debtId"
    }

    // New Routes
    object AddTransaction : Routes("add_transaction/{type}") {
        fun createRoute(type: String) = "add_transaction/$type"
    }
    object Delivery : Routes("delivery")

    // Settings Routes
    object PaymentMethods : Routes("payment_methods")
    object Security : Routes("security")
    object HelpSupport : Routes("help_support")
    object AppSettings : Routes("app_settings")

}
