package com.example.pagaapp.navigation

sealed class Routes(val route: String) {

    object Home : Routes("home")
    object Expenses : Routes("expenses")
    object Location : Routes("location")
    object Tracking : Routes("tracking")
    object History : Routes("history")
    object Profile : Routes("profile")

}