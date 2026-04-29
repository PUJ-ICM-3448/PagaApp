package com.example.pagaapp.ui.screens.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.ui.screens.expenses.DebtItem
import com.example.pagaapp.ui.screens.expenses.DebtStatus
import com.example.pagaapp.ui.screens.expenses.ExpensesViewModel
import com.example.pagaapp.ui.screens.login.AuthViewModel
import com.example.pagaapp.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistoryViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val user = auth.currentUser ?: return
        
        db.collection("users").document(user.uid).collection("history")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val transactions = snapshot?.documents?.mapNotNull { it.toObject(HistoryModel::class.java) } ?: emptyList()
                updateTransactions(transactions, _uiState.value.selectedFilter)
            }
    }

    /**
     * Adds a transaction to Firestore history AND creates a corresponding
     * debt entry in the appropriate collection (youOwe / owedToYou) so it
     * appears in the Debt Management screen.
     */
    fun addTransaction(transaction: HistoryModel, context: Context? = null) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                // 1. Save to the history collection
                db.collection("users").document(user.uid).collection("history")
                    .add(transaction)
                    .await()

                // 2. Create the corresponding debt entry so it shows in Debt Management
                val currentUserData = AuthViewModel.currentUserValue
                val userName = currentUserData?.name ?: "User"
                val userInitials = currentUserData?.initials ?: "U"

                val debtItem = DebtItem(
                    name = transaction.title,
                    description = transaction.category,
                    date = transaction.date,
                    amount = kotlin.math.abs(transaction.amount),
                    status = DebtStatus.PENDING,
                    initials = userInitials,
                    avatarColor = if (transaction.type == TransactionType.EXPENSE)
                        0xFFEF4444 // Red for expenses
                    else
                        0xFF10B981 // Green for income
                )

                // EXPENSE → goes to "youOwe" (you owe this money)
                // INCOME  → goes to "owedToYou" (someone owes you)
                val collection = if (transaction.type == TransactionType.EXPENSE) "youOwe" else "owedToYou"

                db.collection("users").document(user.uid).collection(collection)
                    .add(debtItem)
                    .await()

                // 3. Notify the user
                context?.let {
                    val notifTitle = if (transaction.type == TransactionType.EXPENSE)
                        "Expense Registered" else "Income Registered"
                    val notifBody = "${transaction.title}: $${String.format("%.2f", kotlin.math.abs(transaction.amount))}"
                    NotificationHelper.showNotification(it, notifTitle, notifBody)
                }
            } catch (e: Exception) {
                context?.let {
                    NotificationHelper.showNotification(
                        it,
                        "Error",
                        "Failed to save transaction: ${e.message}"
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: String) {
        updateTransactions(_uiState.value.transactions, filter)
    }

    private fun updateTransactions(allTransactions: List<HistoryModel>, filter: String) {
        val filtered = when (filter) {
            "Income" -> allTransactions.filter { it.type == TransactionType.INCOME }
            "Expense" -> allTransactions.filter { it.type == TransactionType.EXPENSE }
            else -> allTransactions
        }

        val income = allTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = allTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { kotlin.math.abs(it.amount) }

        _uiState.update { it.copy(
            transactions = allTransactions,
            filteredTransactions = filtered,
            selectedFilter = filter,
            totalIncome = income,
            totalExpense = expense
        ) }
    }
}
