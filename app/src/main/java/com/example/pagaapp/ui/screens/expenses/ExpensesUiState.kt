package com.example.pagaapp.ui.screens.expenses

import android.net.Uri
import androidx.compose.ui.graphics.Color

data class DebtItem(
    val id: String,
    val name: String,
    val description: String,
    val date: String,
    val amount: Double,
    val status: DebtStatus,
    val initials: String,
    val avatarColor: Color
)

enum class DebtStatus {
    PENDING, PAID
}

data class ExpensesUiState(
    val youOweList: List<DebtItem> = emptyList(),
    val owedToYouList: List<DebtItem> = emptyList(),
    val evidenceUri: Uri? = null,
    val isUploading: Boolean = false
) {
    val totalYouOwe: Double = youOweList.sumOf { it.amount }
    val totalOwedToYou: Double = owedToYouList.sumOf { it.amount }
}
