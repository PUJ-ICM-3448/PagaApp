package com.example.pagaapp.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Home : Routes("home")
    object Expenses : Routes("expenses")
    object Location : Routes("location")
    object RequestCash : Routes("request_cash")
    object TrackingRequest : Routes("tracking_request/{solicitudId}") {
        fun createRoute(solicitudId: String) = "tracking_request/$solicitudId"
    }
    object Tracking : Routes("tracking")
    object History : Routes("history")
    object Profile : Routes("profile")
    object Repartidor : Routes("repartidor")
    object DeliveryMap : Routes("delivery_map/{solicitudId}") {
        fun createRoute(solicitudId: String) = "delivery_map/$solicitudId"
    }
    object RegisterPayment : Routes("register_payment/{debtId}") {
        fun createRoute(debtId: String) = "register_payment/$debtId"
    }
}
