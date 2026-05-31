package com.example.pagaapp.ui.screens.cash

data class RequestCashUiState(
    val monto: String = "",
    val isLoading: Boolean = false,
    val solicitudId: String? = null,
    val error: String? = null
)
