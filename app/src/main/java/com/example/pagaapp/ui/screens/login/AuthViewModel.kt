package com.example.pagaapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserData(
    val name: String = "",
    val email: String = "",
    val initials: String = ""
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private val _currentUser = MutableStateFlow<UserData?>(null)
        val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()
        
        // Mantengo el acceso directo para compatibilidad rápida si es necesario, 
        // pero lo ideal es usar el Flow.
        val currentUserValue: UserData? get() = _currentUser.value
    }

    init {
        // Restaurar sesión si existe
        auth.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val doc = db.collection("users").document(firebaseUser.uid).get().await()
                    var userData = doc.toObject(UserData::class.java)
                    
                    if (userData != null && userData.initials.isEmpty() && userData.name.isNotEmpty()) {
                        val initials = userData.name.split(" ").filter { it.isNotEmpty() }.let {
                            if (it.size > 1) "${it[0][0]}${it[1][0]}".uppercase()
                            else "${it[0][0]}".uppercase()
                        }
                        userData = userData.copy(initials = initials)
                    }
                    
                    _currentUser.value = userData ?: UserData(
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        initials = "U"
                    )
                } catch (e: Exception) {
                    // Si falla cargar el perfil, al menos tenemos el email
                    _currentUser.value = UserData(
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        initials = "U"
                    )
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, confirmPasswordError = null)
    }

    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (validateLogin()) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    viewModelScope.launch {
                        try {
                            val uid = result.user?.uid ?: throw Exception("No UID")
                            val doc = db.collection("users").document(uid).get().await()
                            var userData = doc.toObject(UserData::class.java)
                            
                            if (userData == null) {
                                userData = UserData(
                                    name = result.user?.displayName ?: "User",
                                    email = result.user?.email ?: "",
                                    initials = "U"
                                )
                            }
                            
                            _currentUser.value = userData
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onSuccess()
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Error loading profile: ${e.message}"
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Invalid email or password"
                    )
                }
        }
    }

    fun loginWithSocial(platform: String, onSuccess: () -> Unit) {
        // Esta función requeriría integración con SDKs específicos (Google, Facebook, etc.)
        // Por ahora simulamos el éxito pero notificamos que falta implementación real
        _uiState.value = _uiState.value.copy(errorMessage = "$platform login not implemented with Firebase yet")
    }

    fun register(onSuccess: () -> Unit) {
        if (validateRegister()) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val email = _uiState.value.email
            val password = _uiState.value.password
            val name = _uiState.value.name
            
            val initials = if (name.isNotEmpty()) {
                name.split(" ").filter { it.isNotEmpty() }.let {
                    if (it.size > 1) "${it[0][0]}${it[1][0]}".uppercase()
                    else "${it[0][0]}".uppercase()
                }
            } else "U"

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val newUser = UserData(name, email, initials)
                    
                    // Guardar en Firestore
                    db.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            _currentUser.value = newUser
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "User created but profile failed: ${e.message}"
                            )
                        }
                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed"
                    )
                }
        }
    }

    private fun validateLogin(): Boolean {
        var isValid = true
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Enter a valid email")
            isValid = false
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        }
        return isValid
    }

    private fun validateRegister(): Boolean {
        var isValid = validateLogin()
        val name = _uiState.value.name
        val confirmPassword = _uiState.value.confirmPassword
        val password = _uiState.value.password

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            isValid = false
        }
        if (confirmPassword != password) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }
        return isValid
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout(onLogout: () -> Unit) {
        auth.signOut()
        _uiState.value = AuthUiState()
        _currentUser.value = null
        onLogout()
    }
}

data class AuthUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
