package com.example.pagaapp.ui.screens.location

enum class LocationType {
    ATM,
    BANK,
    STORE,
    PARTNER
}

data class LocationModel(
    val name: String,
    val typeLabel: String,
    val address: String,
    val distance: String,
    val type: LocationType
)