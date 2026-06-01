package com.example.pagaapp.ui.screens.home

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Usuario"
                    val initials = name.split(" ").filter { it.isNotEmpty() }
                        .map { it[0] }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    _uiState.update {
                        it.copy(
                            userName = name,
                            userInitials = initials,
                            isLoading = false
                        )
                    }
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(isLoading = false) }
            }
    }
}
