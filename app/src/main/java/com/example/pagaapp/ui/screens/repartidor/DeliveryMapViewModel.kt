package com.example.pagaapp.ui.screens.repartidor

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.data.network.DirectionsRetrofitClient
import com.example.pagaapp.util.decodePolyline
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeliveryMapViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(DeliveryMapUiState())
    val uiState: StateFlow<DeliveryMapUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var solicitudId: String? = null

    private val GOOGLE_MAPS_API_KEY = "AIzaSyAPXZetb9acvDrOrGcOUUOiCdir59np-Cw"

    private val fallbackLat = 4.6156
    private val fallbackLon = -74.0690

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
                    val clienteLat = snapshot.getDouble("clienteLatitud") ?: 0.0
                    val clienteLon = snapshot.getDouble("clienteLongitud") ?: 0.0
                    val repartidorLat = snapshot.getDouble("repartidorLatitud") ?: fallbackLat
                    val repartidorLon = snapshot.getDouble("repartidorLongitud") ?: fallbackLon

                    _uiState.update {
                        it.copy(
                            clienteLatitud = clienteLat,
                            clienteLongitud = clienteLon,
                            clienteNombre = snapshot.getString("clienteNombre") ?: "Cliente",
                            monto = snapshot.getString("monto") ?: "0",
                            estado = snapshot.getString("estado") ?: "",
                            repartidorLatitud = repartidorLat,
                            repartidorLongitud = repartidorLon,
                            isLoading = false
                        )
                    }
                    
                    if (clienteLat != 0.0 && repartidorLat != 0.0) {
                        fetchRoute(repartidorLat, repartidorLon, clienteLat, clienteLon)
                    }
                }
            }
        
        startLocationUpdates()
    }

    private fun fetchRoute(origLat: Double, origLon: Double, destLat: Double, destLon: Double) {
        viewModelScope.launch {
            try {
                val origin = "$origLat,$origLon"
                val destination = "$destLat,$destLon"
                val response = DirectionsRetrofitClient.instance.getDirections(origin, destination, GOOGLE_MAPS_API_KEY)
                if (response.routes.isNotEmpty()) {
                    val points = response.routes[0].overview_polyline.points
                    val decodedPath = decodePolyline(points)
                    _uiState.update { it.copy(routePoints = decodedPath) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val lat = if (location.latitude != 0.0) location.latitude else fallbackLat
                    val lon = if (location.longitude != 0.0) location.longitude else fallbackLon
                    
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
