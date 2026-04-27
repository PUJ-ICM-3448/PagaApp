package com.example.pagaapp.ui.screens.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserData(
    val name: String,
    val email: String,
    val initials: String
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    companion object {
        // Base de datos de credenciales
        private val registeredUsers = mutableMapOf<String, String>(
            "test@pagaapp.com" to "123456"
        )
        
        // Base de datos de información de perfil
        private val userProfiles = mutableMapOf<String, UserData>(
            "test@pagaapp.com" to UserData("Carlos Rodriguez", "test@pagaapp.com", "CR")
        )
        
        // Usuario actualmente logueado
        var currentUser: UserData? = null
            private set
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
            
            if (registeredUsers[email] == password) {
                currentUser = userProfiles[email]
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid email or password"
                )
            }
        }
    }

    fun loginWithSocial(platform: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Simulamos login social
        val socialEmail = "social@${platform.lowercase()}.com"
        val socialName = "User $platform"
        val initials = platform.first().uppercase()

        val socialUser = UserData(socialName, socialEmail, initials)
        
        // Registramos si no existe
        if (!registeredUsers.containsKey(socialEmail)) {
            registeredUsers[socialEmail] = "social_pass"
            userProfiles[socialEmail] = socialUser
        }

        currentUser = socialUser
        _uiState.value = _uiState.value.copy(isLoading = false)
        onSuccess()
    }

    fun register(onSuccess: () -> Unit) {
        if (validateRegister()) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val email = _uiState.value.email
            val name = _uiState.value.name
            val initials = if (name.isNotEmpty()) {
                name.split(" ").filter { it.isNotEmpty() }.let {
                    if (it.size > 1) "${it[0][0]}${it[1][0]}".uppercase()
                    else "${it[0][0]}".uppercase()
                }
            } else "U"

            // Guardamos credenciales
            registeredUsers[email] = _uiState.value.password
            
            // Creamos y guardamos el perfil
            val newUser = UserData(name, email, initials)
            userProfiles[email] = newUser
            currentUser = newUser
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
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
        _uiState.value = AuthUiState()
        currentUser = null
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
