package com.example.pagaapp.ui.screens.expenses

import androidx.compose.ui.graphics.Color

data class DebtItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val status: DebtStatus = DebtStatus.PENDING,
    val initials: String = "",
    val avatarColor: Long = 0xFF10B981 // Store color as Long
)

enum class DebtStatus {
    PENDING, PAID
}

data class ExpensesUiState(
    val youOweList: List<DebtItem> = emptyList(),
    val owedToYouList: List<DebtItem> = emptyList()
) {
    // Solo sumamos lo que está pendiente para que el total sea real
    val totalYouOwe: Double = youOweList.filter { it.status == DebtStatus.PENDING }.sumOf { it.amount }
    val totalOwedToYou: Double = owedToYouList.filter { it.status == DebtStatus.PENDING }.sumOf { it.amount }
}
