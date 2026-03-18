package com.example.pagaapp.ui.screens.expenses

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpensesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExpensesUiState(
        youOweList = listOf(
            DebtItem(
                id = "1",
                name = "Maria Garcia",
                description = "Dinner at Restaurant",
                date = "March 8, 2026",
                amount = 45.50,
                status = DebtStatus.PENDING,
                initials = "MG",
                avatarColor = Color(0xFF10B981)
            ),
            DebtItem(
                id = "2",
                name = "Sofia Martinez",
                description = "Coffee shop",
                date = "March 3, 2026",
                amount = 15.75,
                status = DebtStatus.PAID,
                initials = "SM",
                avatarColor = Color(0xFF10B981)
            ),
            DebtItem(
                id = "4",
                name = "Carlos Ruiz",
                description = "Rent share",
                date = "March 1, 2026",
                amount = 200.00,
                status = DebtStatus.PENDING,
                initials = "CR",
                avatarColor = Color(0xFF3B82F6)
            ),
            DebtItem(
                id = "5",
                name = "Elena Gomez",
                description = "Gym subscription",
                date = "Feb 28, 2026",
                amount = 30.00,
                status = DebtStatus.PAID,
                initials = "EG",
                avatarColor = Color(0xFFF59E0B)
            )
        ),
        owedToYouList = listOf(
            DebtItem(
                id = "3",
                name = "Juan Lopez",
                description = "Movie tickets",
                date = "March 5, 2026",
                amount = 28.00,
                status = DebtStatus.PENDING,
                initials = "JL",
                avatarColor = Color(0xFF10B981)
            ),
            DebtItem(
                id = "6",
                name = "Pedro Sanchez",
                description = "Pizza night",
                date = "March 10, 2026",
                amount = 12.50,
                status = DebtStatus.PENDING,
                initials = "PS",
                avatarColor = Color(0xFF8B5CF6)
            ),
            DebtItem(
                id = "7",
                name = "Ana Rodriguez",
                description = "Gasoline",
                date = "March 12, 2026",
                amount = 40.00,
                status = DebtStatus.PENDING,
                initials = "AR",
                avatarColor = Color(0xFFEC4899)
            )
        )
    ))
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()
}
