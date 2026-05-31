package com.example.pagaapp.ui.screens.cash

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RequestCashViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(RequestCashUiState())
    val uiState: StateFlow<RequestCashUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Fallback solo si la ubicación real falla completamente (Coordenadas Javeriana)
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

        // Intentar obtener la ubicación más precisa posible
        fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            ?.addOnSuccessListener { location ->
                if (location != null) {
                    crearSolicitud(currentUser.uid, currentUser.displayName, monto, location.latitude, location.longitude)
                } else {
                    // Si getCurrentLocation es null, intentar con lastLocation
                    fusedLocationClient?.lastLocation?.addOnSuccessListener { lastLoc ->
                        val lat = lastLoc?.latitude ?: fallbackLat
                        val lon = lastLoc?.longitude ?: fallbackLon
                        crearSolicitud(currentUser.uid, currentUser.displayName, monto, lat, lon)
                    }?.addOnFailureListener {
                        crearSolicitud(currentUser.uid, currentUser.displayName, monto, fallbackLat, fallbackLon)
                    }
                }
            }?.addOnFailureListener {
                crearSolicitud(currentUser.uid, currentUser.displayName, monto, fallbackLat, fallbackLon)
            } ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Cliente de ubicación no configurado") }
            }
    }

    private fun crearSolicitud(uid: String, nombre: String?, monto: String, lat: Double, lon: Double) {
        val clienteNombre = nombre ?: auth.currentUser?.email?.split("@")?.get(0) ?: "Cliente"
        
        val solicitud = hashMapOf(
            "clienteId" to uid,
            "clienteNombre" to clienteNombre,
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
