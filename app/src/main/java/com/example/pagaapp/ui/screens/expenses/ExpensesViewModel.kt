package com.example.pagaapp.ui.screens.expenses

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.ui.screens.history.HistoryModel
import com.example.pagaapp.ui.screens.history.TransactionType
import com.example.pagaapp.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpensesViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        val debtsState = MutableStateFlow(ExpensesUiState(emptyList(), emptyList()))
        val historyTransactions = MutableStateFlow<List<HistoryModel>>(emptyList())
    }

    private val _uiState = debtsState
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val user = auth.currentUser ?: return
        
        // Load You Owe (expenses)
        db.collection("users").document(user.uid).collection("youOwe")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(DebtItem::class.java)?.copy(id = it.id) } ?: emptyList()
                debtsState.update { it.copy(youOweList = list) }
            }

        // Load Owed To You (income/debts from others)
        db.collection("users").document(user.uid).collection("owedToYou")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(DebtItem::class.java)?.copy(id = it.id) } ?: emptyList()
                debtsState.update { it.copy(owedToYouList = list) }
            }

        // Load History
        db.collection("users").document(user.uid).collection("history")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(HistoryModel::class.java) } ?: emptyList()
                historyTransactions.value = list
            }
    }

    fun registerPayment(context: Context, debtId: String, amount: Double, method: String, imageUri: String? = null) {
        val user = auth.currentUser ?: return
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

            viewModelScope.launch {
                try {
                    // Update debt status in Firestore
                    val collection = if (currentState.youOweList.any { d -> d.id == debtId }) "youOwe" else "owedToYou"
                    db.collection("users").document(user.uid).collection(collection).document(debtId)
                        .update("status", DebtStatus.PAID)

                    // Add to history in Firestore
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
                    
                    db.collection("users").document(user.uid).collection("history")
                        .add(newTransaction)
                    
                    // Notify user
                    NotificationHelper.showNotification(
                        context, 
                        "Payment Successful", 
                        "You paid $${String.format("%.2f", amount)} to ${it.name} via $method"
                    )
                } catch (e: Exception) {
                    NotificationHelper.showNotification(
                        context, 
                        "Error", 
                        "Failed to register payment: ${e.message}"
                    )
                }
            }
        } ?: run {
            NotificationHelper.showNotification(
                context, 
                "Error", 
                "Could not find the debt information."
            )
        }
    }
}
