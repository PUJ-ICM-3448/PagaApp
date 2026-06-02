package com.example.pagaapp.ui.screens.home

data class HomeUiState(
    val userName: String = "",
    val userInitials: String = "",
    val balance: Double = 152.75,
    val debts: Double = 89.25,
    val owedToMe: Double = 242.00,
    val isLoading: Boolean = false,
    val countryCapital: String = "",
    val countryCurrency: String = ""
)
