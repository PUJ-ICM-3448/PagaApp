package com.example.pagaapp.ui.screens.profile

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun updateProfileImage(bitmap: Bitmap) {
        _uiState.update { it.copy(profileBitmap = bitmap) }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Usuario"
                    val email = document.getString("email") ?: auth.currentUser?.email ?: ""
                    val initials = name.split(" ").filter { it.isNotEmpty() }
                        .map { it[0] }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    _uiState.update {
                        it.copy(
                            profile = ProfileModel(
                                name = name,
                                email = email,
                                initials = initials,
                                memberSince = "January 2024",
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
            }
    }
}
