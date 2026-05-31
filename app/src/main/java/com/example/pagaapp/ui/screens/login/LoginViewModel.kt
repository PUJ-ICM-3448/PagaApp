package com.example.pagaapp.ui.screens.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val role: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult.Error("Email and password cannot be empty")
            return
        }

        _loginResult.value = LoginResult.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    fetchUserRole(uid)
                } else {
                    _loginResult.value = LoginResult.Error("User not found")
                }
            }
            .addOnFailureListener {
                _loginResult.value = LoginResult.Error(it.message ?: "Authentication failed")
            }
    }

    private fun fetchUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "cliente"
                _loginResult.value = LoginResult.Success(role)
            }
            .addOnFailureListener {
                _loginResult.value = LoginResult.Error("Failed to fetch user role")
            }
    }

    fun resetResult() {
        _loginResult.value = LoginResult.Idle
    }
}
