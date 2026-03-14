package com.example.pagaapp.ui.screens.home

data class HomeUiState(
    val balance: Double = 0.0,
    val debts: Double = 0.0,
    val owedToMe: Double = 0.0
)