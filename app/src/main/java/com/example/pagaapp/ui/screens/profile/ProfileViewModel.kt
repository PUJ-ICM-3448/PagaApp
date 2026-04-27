package com.example.pagaapp.ui.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.lifecycle.ViewModel
import com.example.pagaapp.ui.screens.login.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val currentUser = AuthViewModel.currentUser
        
        val profileName = currentUser?.name ?: "Carlos Rodriguez"
        val profileEmail = currentUser?.email ?: "carlos.rodriguez@email.com"
        val profileInitials = currentUser?.initials ?: "CR"

        _uiState.value = ProfileUiState(
            profile = ProfileModel(
                name = profileName,
                email = profileEmail,
                initials = profileInitials,
                memberSince = "January 2026",
                totalTransactions = 47,
                activeFriends = 12,
                totalShared = 847,
                expenses = 32,
                settings = listOf(
                    ProfileSettingModel(
                        title = "Payment Methods",
                        icon = Icons.Outlined.CreditCard
                    ),
                    ProfileSettingModel(
                        title = "Location Preferences",
                        icon = Icons.Outlined.LocationOn
                    ),
                    ProfileSettingModel(
                        title = "Security and Verification",
                        icon = Icons.Outlined.Shield
                    ),
                    ProfileSettingModel(
                        title = "Help & Support",
                        icon = Icons.Outlined.HelpOutline
                    ),
                    ProfileSettingModel(
                        title = "App Settings",
                        icon = Icons.Outlined.Settings
                    )
                )
            )
        )
    }
}
