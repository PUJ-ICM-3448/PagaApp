package com.example.pagaapp.ui.screens.expenses

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.pagaapp.ui.screens.history.HistoryModel
import com.example.pagaapp.ui.screens.history.TransactionType
import com.example.pagaapp.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class ExpensesViewModel : ViewModel() {
    
    // Singleton-like approach for debts state so it's shared between screens
    companion object {
        private val initialYouOwe = listOf(
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
        )

        private val initialOwedToYou = listOf(
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

        val debtsState = MutableStateFlow(ExpensesUiState(initialYouOwe, initialOwedToYou))
        val historyTransactions = MutableStateFlow<List<HistoryModel>>(emptyList())
    }

    private val _uiState = debtsState
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    fun registerPayment(context: Context, debtId: String, amount: Double, method: String, imageUri: String? = null) {
        val currentState = _uiState.value
        val debt = currentState.youOweList.find { it.id == debtId }
            ?: currentState.owedToYouList.find { it.id == debtId }

        debt?.let {
            if (amount <= 0) {
                NotificationHelper.showNotification(
                    context, 
                    "Payment Failed", 
                    "Invalid amount for payment to ${it.name}."
                )
                return
            }

            // Update UI state locally
            debtsState.update { state ->
                val updatedYouOweList = state.youOweList.map { item ->
                    if (item.id == debtId) item.copy(status = DebtStatus.PAID) else item
                }
                val updatedOwedToYouList = state.owedToYouList.map { item ->
                    if (item.id == debtId) item.copy(status = DebtStatus.PAID) else item
                }
                state.copy(
                    youOweList = updatedYouOweList,
                    owedToYouList = updatedOwedToYouList
                )
            }
            
            // Add to history
            val sdf = SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault())
            val currentDate = sdf.format(Date())
            
            val newTransaction = HistoryModel(
                title = "Payment to ${it.name}",
                category = "Payment",
                date = currentDate,
                amount = -amount,
                type = TransactionType.EXPENSE,
                imageUri = imageUri
            )
            
            historyTransactions.value = listOf(newTransaction) + historyTransactions.value
            
            // Notify user
            NotificationHelper.showNotification(
                context, 
                "Payment Successful", 
                "You paid $${String.format("%.2f", amount)} to ${it.name} via $method"
            )
        } ?: run {
            NotificationHelper.showNotification(
                context, 
                "Error", 
                "Could not find the debt information."
            )
        }
    }
}
