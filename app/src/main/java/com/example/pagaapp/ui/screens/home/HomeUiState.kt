package com.example.pagaapp.ui.screens.home

data class HomeUiState(
    val userName: String = "",
    val userInitials: String = "",
    val balance: Double = 152.75, // Mantener por ahora como demo o fetch de otra tabla
    val debts: Double = 89.25,
    val owedToMe: Double = 242.00,
    val isLoading: Boolean = false
)
