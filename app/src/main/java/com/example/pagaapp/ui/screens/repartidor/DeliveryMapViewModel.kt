package com.example.pagaapp.ui.screens.repartidor

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DeliveryMapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(DeliveryMapUiState())
    val uiState: StateFlow<DeliveryMapUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var solicitudId: String? = null

    // Coordenadas de demo: Universidad Javeriana Bogotá
    private val defaultLat = 4.6280
    private val defaultLon = -74.0647

    fun setLocationClient(client: FusedLocationProviderClient) {
        fusedLocationClient = client
    }

    fun startTracking(id: String) {
        this.solicitudId = id
        _uiState.update { it.copy(isLoading = true) }

        db.collection("solicitudesEfectivo").document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val clienteLat = snapshot.getDouble("clienteLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val clienteLon = snapshot.getDouble("clienteLongitud").let { if (it == null || it == 0.0) defaultLon else it }
                    
                    _uiState.update {
                        it.copy(
                            clienteLatitud = clienteLat,
                            clienteLongitud = clienteLon,
                            clienteNombre = snapshot.getString("clienteNombre") ?: "",
                            monto = snapshot.getString("monto") ?: "",
                            estado = snapshot.getString("estado") ?: "",
                            repartidorLatitud = snapshot.getDouble("repartidorLatitud") ?: defaultLat,
                            repartidorLongitud = snapshot.getDouble("repartidorLongitud") ?: defaultLon,
                            isLoading = false
                        )
                    }
                }
            }
        
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val lat = if (location.latitude != 0.0) location.latitude else defaultLat
                    val lon = if (location.longitude != 0.0) location.longitude else defaultLon
                    
                    updateLocationInFirestore(lat, lon)
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(request, locationCallback!!, null)
    }

    private fun updateLocationInFirestore(lat: Double, lon: Double) {
        val id = solicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update(
                mapOf(
                    "repartidorLatitud" to lat,
                    "repartidorLongitud" to lon,
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }

    fun actualizarEstado(nuevoEstado: String) {
        val id = solicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", nuevoEstado)
    }

    fun finalizarPedido(onFinished: () -> Unit) {
        val id = solicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", "entregado")
            .addOnSuccessListener {
                stopLocationUpdates()
                onFinished()
            }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
