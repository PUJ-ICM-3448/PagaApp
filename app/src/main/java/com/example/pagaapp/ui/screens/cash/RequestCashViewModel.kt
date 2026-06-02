package com.example.pagaapp.ui.screens.cash

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RequestCashViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(RequestCashUiState())
    val uiState: StateFlow<RequestCashUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Fallback coordinates (Javeriana) ONLY if real location fails completely
    private val fallbackLat = 4.6280
    private val fallbackLon = -74.0647

    fun onMontoChange(monto: String) {
        _uiState.update { it.copy(monto = monto) }
    }

    fun setLocationClient(client: FusedLocationProviderClient) {
        fusedLocationClient = client
    }

    @SuppressLint("MissingPermission")
    fun solicitarEfectivo() {
        val monto = _uiState.value.monto
        if (monto.isBlank()) {
            _uiState.update { it.copy(error = "Ingrese un monto válido") }
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        // Fetch real user name from Firestore
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val realName = document.getString("name") ?: currentUser.displayName ?: "Usuario"
                
                // Use getCurrentLocation for higher accuracy on real device
                fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    ?.addOnSuccessListener { location ->
                        if (location != null) {
                            crearSolicitud(currentUser.uid, realName, monto, location.latitude, location.longitude)
                        } else {
                            // Try last location as secondary fallback
                            fusedLocationClient?.lastLocation?.addOnSuccessListener { lastLoc ->
                                val lat = lastLoc?.latitude ?: fallbackLat
                                val lon = lastLoc?.longitude ?: fallbackLon
                                crearSolicitud(currentUser.uid, realName, monto, lat, lon)
                            }?.addOnFailureListener {
                                crearSolicitud(currentUser.uid, realName, monto, fallbackLat, fallbackLon)
                            }
                        }
                    }?.addOnFailureListener {
                        crearSolicitud(currentUser.uid, realName, monto, fallbackLat, fallbackLon)
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false, error = "GPS no disponible") }
                    }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(isLoading = false, error = "Error al obtener perfil") }
            }
    }

    private fun crearSolicitud(uid: String, nombre: String, monto: String, lat: Double, lon: Double) {
        val solicitud = hashMapOf(
            "clienteId" to uid,
            "clienteNombre" to nombre,
            "monto" to monto,
            "clienteLatitud" to lat,
            "clienteLongitud" to lon,
            "estado" to "pendiente",
            "repartidorId" to "",
            "repartidorNombre" to "",
            "repartidorLatitud" to 0.0,
            "repartidorLongitud" to 0.0,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("solicitudesEfectivo")
            .add(solicitud)
            .addOnSuccessListener { docRef ->
                _uiState.update { it.copy(isLoading = false, solicitudId = docRef.id) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
    }
}
