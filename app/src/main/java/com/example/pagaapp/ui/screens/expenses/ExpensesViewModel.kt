package com.example.pagaapp.ui.screens.expenses

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.example.pagaapp.ui.screens.history.HistoryModel
import com.example.pagaapp.ui.screens.history.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

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

    // Singleton-like approach for history demo (in a real app use a Repository/DB)
    companion object {
        val historyTransactions = MutableStateFlow<List<HistoryModel>>(emptyList())
    }

    fun registerPayment(context: Context, debtId: String, amount: Double, method: String) {
        val debt = _uiState.value.youOweList.find { it.id == debtId }
            ?: _uiState.value.owedToYouList.find { it.id == debtId }

        debt?.let {
            // Update UI state locally (simulated)
            val updatedYouOweList = _uiState.value.youOweList.map { item ->
                if (item.id == debtId) item.copy(status = DebtStatus.PAID) else item
            }
            val updatedOwedToYouList = _uiState.value.owedToYouList.map { item ->
                if (item.id == debtId) item.copy(status = DebtStatus.PAID) else item
            }
            
            _uiState.value = _uiState.value.copy(
                youOweList = updatedYouOweList,
                owedToYouList = updatedOwedToYouList
            )
            
            // Add to history
            val sdf = SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault())
            val currentDate = sdf.format(Date())
            
            val newTransaction = HistoryModel(
                title = "Payment to ${it.name}",
                category = "Payment",
                date = currentDate,
                amount = -amount,
                type = TransactionType.EXPENSE
            )
            
            historyTransactions.value = listOf(newTransaction) + historyTransactions.value
            
            // 1. Notify user
            showNotification(context, "Payment Registered", "You registered a payment of $${String.format("%.2f", amount)} to ${it.name} via $method")
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "payments_channel")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
