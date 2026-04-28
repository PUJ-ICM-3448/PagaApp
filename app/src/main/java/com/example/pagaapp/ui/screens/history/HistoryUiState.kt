package com.example.pagaapp.ui.screens.history

data class HistoryUiState(
    val transactions: List<HistoryModel> = emptyList(),
    val filteredTransactions: List<HistoryModel> = emptyList(),
    val selectedFilter: String = "All",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)
