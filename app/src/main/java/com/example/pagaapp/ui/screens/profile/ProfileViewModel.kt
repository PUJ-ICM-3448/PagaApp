package com.example.pagaapp.ui.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.screens.expenses.ExpensesViewModel
import com.example.pagaapp.ui.screens.history.TransactionType
import com.example.pagaapp.ui.screens.login.AuthViewModel
import com.example.pagaapp.ui.screens.login.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combinamos el flujo del usuario y las transacciones para tener datos siempre actualizados
            combine(
                AuthViewModel.currentUser,
                ExpensesViewModel.historyTransactions
            ) { user, transactions ->
                updateProfileData(user, transactions)
            }.collect { }
        }
    }

    private fun updateProfileData(currentUser: UserData?, transactions: List<com.example.pagaapp.ui.screens.history.HistoryModel>) {
        val profileName = currentUser?.name ?: "Loading..."
        val profileEmail = currentUser?.email ?: "Loading..."
        val profileInitials = currentUser?.initials ?: ".."

        // Estadísticas reales basadas en el historial
        val expensesCount = transactions.count { it.type == TransactionType.EXPENSE }
        val totalSharedAmount = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { kotlin.math.abs(it.amount) }.toInt()
        val activeFriendsCount = 12 // Esto se podría conectar a un FriendsViewModel en el futuro

        _uiState.value = ProfileUiState(
            profile = ProfileModel(
                name = profileName,
                email = profileEmail,
                initials = profileInitials,
                memberSince = "January 2026",
                totalTransactions = transactions.size,
                activeFriends = activeFriendsCount,
                totalShared = totalSharedAmount,
                expenses = expensesCount,
                settings = listOf(
                    ProfileSettingModel(
                        title = "Payment Methods",
                        icon = Icons.Outlined.CreditCard,
                        route = Routes.PaymentMethods.route
                    ),
                    ProfileSettingModel(
                        title = "Location Preferences",
                        icon = Icons.Outlined.LocationOn,
                        route = Routes.Location.route
                    ),
                    ProfileSettingModel(
                        title = "Security and Verification",
                        icon = Icons.Outlined.Shield,
                        route = Routes.Security.route
                    ),
                    ProfileSettingModel(
                        title = "Help & Support",
                        icon = Icons.Outlined.HelpOutline,
                        route = Routes.HelpSupport.route
                    ),
                    ProfileSettingModel(
                        title = "App Settings",
                        icon = Icons.Outlined.Settings,
                        route = Routes.AppSettings.route
                    )
                )
            )
        )
    }
}
