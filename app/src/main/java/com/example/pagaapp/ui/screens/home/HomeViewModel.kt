package com.example.pagaapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchUserData()
        fetchCountryInfo()
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
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(isLoading = false) }
            }
    }

    private fun fetchCountryInfo() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getColombiaInfo()
                if (response.isNotEmpty()) {
                    val country = response[0]
                    val capital = country.capital.firstOrNull() ?: "N/A"
                    val currency = country.currencies.values.firstOrNull()?.name ?: "N/A"
                    
                    _uiState.update {
                        it.copy(
                            countryCapital = capital,
                            countryCurrency = currency
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fail silently for demo or set error in state
            }
        }
    }
}
