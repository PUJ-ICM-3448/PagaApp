package com.example.pagaapp.ui.screens.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = HistoryUiState(
            transactions = listOf(
                HistoryModel(
                    "Payment received",
                    "Payment",
                    "March 9, 2026 • 14:30",
                    28.00,
                    TransactionType.INCOME
                ),
                HistoryModel(
                    "Dinner at Italian",
                    "Food & Dining",
                    "March 8, 2026 • 20:15",
                    -45.50,
                    TransactionType.EXPENSE
                ),
                HistoryModel(
                    "Payment sent to Sofia",
                    "Payment",
                    "March 7, 2026 • 11:00",
                    -15.75,
                    TransactionType.EXPENSE
                ),
                HistoryModel(
                    "Movie tickets",
                    "Entertainment",
                    "March 6, 2026 • 19:00",
                    -28.00,
                    TransactionType.EXPENSE
                )
            )
        )
    }
}