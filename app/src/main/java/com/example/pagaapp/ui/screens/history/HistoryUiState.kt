package com.example.pagaapp.ui.screens.history

data class HistoryUiState(
    val transactions: List<HistoryModel> = emptyList(),
    val selectedFilter: String = "All"
)