package com.example.pagaapp.ui.screens.history

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class HistoryModel(
    val title: String = "",
    val category: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val imageUri: String? = null
)
