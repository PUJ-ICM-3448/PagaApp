package com.example.pagaapp.ui.screens.history

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class HistoryModel(
    val title: String,
    val category: String,
    val date: String,
    val amount: Double,
    val type: TransactionType
)