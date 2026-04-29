package com.example.pagaapp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.ui.screens.expenses.ExpensesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val initialTransactions = listOf(
        HistoryModel("Payment received", "Payment", "March 9, 2026 • 14:30", 28.00, TransactionType.INCOME),
        HistoryModel("Dinner at Italian", "Food & Dining", "March 8, 2026 • 20:15", -45.50, TransactionType.EXPENSE),
        HistoryModel("Payment sent to Sofia", "Payment", "March 7, 2026 • 11:00", -15.75, TransactionType.EXPENSE)
    )

    // Shared list in memory for the session
    companion object {
        private val addedTransactions = MutableStateFlow<List<HistoryModel>>(emptyList())
    }

    init {
        viewModelScope.launch {
            // Combine initial + expenses + newly added transactions
            ExpensesViewModel.historyTransactions.collect { expenses ->
                addedTransactions.collect { added ->
                    val all = added + expenses + initialTransactions
                    updateTransactions(all, _uiState.value.selectedFilter)
                }
            }
        }
    }

    fun addTransaction(transaction: HistoryModel) {
        addedTransactions.update { listOf(transaction) + it }
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
