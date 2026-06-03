package com.example.pagaapp.ui.screens.repartidor

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.data.network.DirectionsRetrofitClient
import com.example.pagaapp.util.NotificationHelper
import com.example.pagaapp.util.decodePolyline
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeliveryMapViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val notificationHelper = NotificationHelper(application)

    private val _uiState = MutableStateFlow(DeliveryMapUiState())
    val uiState: StateFlow<DeliveryMapUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var activeSolicitudId: String? = null
    private var snapshotListener: ListenerRegistration? = null

    private val GOOGLE_MAPS_API_KEY = "AIzaSyAPXZetb9acvDrOrGcOUUOiCdir59np-Cw"

    fun setLocationClient(client: FusedLocationProviderClient) {
        Log.d("DeliveryTracking", "setLocationClient called")
        fusedLocationClient = client
        if (activeSolicitudId != null) {
            startLocationUpdates()
        }
    }

    fun startTracking(id: String) {
        Log.d("DeliveryTracking", "startTracking called with id: $id")
        
        if (activeSolicitudId != null && activeSolicitudId != id) {
            stopLocationUpdates()
            snapshotListener?.remove()
        }

        this.activeSolicitudId = id
        _uiState.update { it.copy(isLoading = true) }

        snapshotListener = db.collection("solicitudesEfectivo").document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DeliveryTracking", "Error ubicación o Firestore", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    if (id != activeSolicitudId) {
                        Log.d("DeliveryTracking", "Ignorando snapshot de solicitud vieja: ${snapshot.id}")
                        return@addSnapshotListener
                    }

                    val estado = snapshot.getString("estado") ?: ""
                    val clienteLat = snapshot.getDouble("clienteLatitud") ?: 0.0
                    val clienteLon = snapshot.getDouble("clienteLongitud") ?: 0.0

                    // Solo ubicación real: si Firestore aún no tiene la posición del
                    // repartidor, se queda en 0.0 y la UI muestra "Buscando ubicación...".
                    val repartidorLat = snapshot.getDouble("repartidorLatitud") ?: 0.0
                    val repartidorLon = snapshot.getDouble("repartidorLongitud") ?: 0.0

                    _uiState.update {
                        it.copy(
                            clienteLatitud = clienteLat,
                            clienteLongitud = clienteLon,
                            clienteNombre = snapshot.getString("clienteNombre") ?: "Cliente",
                            monto = snapshot.getString("monto") ?: "0",
                            estado = estado,
                            repartidorLatitud = repartidorLat,
                            repartidorLongitud = repartidorLon,
                            isLoading = false
                        )
                    }

                    if (clienteLat != 0.0 && repartidorLat != 0.0) {
                        fetchRoute(repartidorLat, repartidorLon, clienteLat, clienteLon)
                    }

                    if (estado == "entregado") {
                        Log.d("DeliveryTracking", "Estado es 'entregado' para $id, deteniendo actualizaciones")
                        stopLocationUpdates()
                    } else if (estado == "aceptado" || estado == "en_camino") {
                        if (locationCallback == null && fusedLocationClient != null) {
                            startLocationUpdates()
                        }
                    }
                }
            }

        if (fusedLocationClient != null) {
            startLocationUpdates()
        }
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
                Log.e("DeliveryTracking", "Error al obtener ruta", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val solicitudId = activeSolicitudId
        Log.d("DeliveryTracking", "startLocationUpdates llamado solicitudId=$solicitudId")

        if (fusedLocationClient == null || solicitudId == null) {
            return
        }

        if (locationCallback != null) return

        fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            ?.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("DeliveryTracking", "Ubicación inicial recibida: ${location.latitude}, ${location.longitude}")
                    updateLocationInFirestore(location.latitude, location.longitude)
                }
            }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.locations.lastOrNull()
                if (location != null) {
                    Log.d("DeliveryTracking", "Nueva ubicación: ${location.latitude}, ${location.longitude}")
                    updateLocationInFirestore(location.latitude, location.longitude)
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                request,
                locationCallback!!,
                android.os.Looper.getMainLooper()
            )
            Log.d("DeliveryTracking", "requestLocationUpdates ejecutado exitosamente para $solicitudId")
        } catch (e: Exception) {
            Log.e("DeliveryTracking", "Error al iniciar updates", e)
        }
    }

    private fun updateLocationInFirestore(lat: Double, lon: Double) {
        val id = activeSolicitudId ?: return
        Log.d("DeliveryTracking", "Actualizando Firestore: $id lat=$lat, lon=$lon")

        db.collection("solicitudesEfectivo").document(id)
            .update(
                mapOf(
                    "repartidorLatitud" to lat,
                    "repartidorLongitud" to lon,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                Log.d("DeliveryTracking", "Firestore actualizado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e("DeliveryTracking", "Error ubicación o Firestore", e)
            }
    }

    fun actualizarEstado(nuevoEstado: String) {
        val id = activeSolicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                if (nuevoEstado == "en_camino") {
                    notificationHelper.showNotification("Repartidor en camino", "El repartidor va hacia el cliente.")
                }
            }
    }

    fun finalizarPedido(onFinished: () -> Unit) {
        val id = activeSolicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", "entregado")
            .addOnSuccessListener {
                notificationHelper.showNotification("Pedido entregado", "La entrega fue finalizada correctamente.")
                stopLocationUpdates()
                onFinished()
            }
    }

    private fun stopLocationUpdates() {
        Log.d("DeliveryTracking", "stopLocationUpdates solicitado")
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
            Log.d("DeliveryTracking", "Callback removido")
        }
        locationCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
        stopLocationUpdates()
        activeSolicitudId = null
    }
}
