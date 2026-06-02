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
import com.google.firebase.firestore.FirebaseFirestore
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
    private var solicitudId: String? = null

    private val GOOGLE_MAPS_API_KEY = "AIzaSyAPXZetb9acvDrOrGcOUUOiCdir59np-Cw"

    // Coordenadas de demo: Museo Nacional (Fallback Repartidor)
    private val fallbackLat = 4.6156
    private val fallbackLon = -74.0690

    fun setLocationClient(client: FusedLocationProviderClient) {
        Log.d("DeliveryTracking", "setLocationClient called")
        fusedLocationClient = client
        if (solicitudId != null) {
            startLocationUpdates()
        }
    }

    fun startTracking(id: String) {
        Log.d("DeliveryTracking", "startTracking called with id: $id")
        this.solicitudId = id
        _uiState.update { it.copy(isLoading = true) }

        db.collection("solicitudesEfectivo").document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DeliveryTracking", "SnapshotListener error", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val estado = snapshot.getString("estado") ?: ""
                    val clienteLat = snapshot.getDouble("clienteLatitud") ?: 0.0
                    val clienteLon = snapshot.getDouble("clienteLongitud") ?: 0.0
                    
                    // Obtener ubicación de Firestore
                    val firestoreRepLat = snapshot.getDouble("repartidorLatitud") ?: 0.0
                    val firestoreRepLon = snapshot.getDouble("repartidorLongitud") ?: 0.0
                    
                    // Si Firestore tiene 0, mostrar fallback solo visualmente
                    val repartidorLat = if (firestoreRepLat != 0.0) firestoreRepLat else fallbackLat
                    val repartidorLon = if (firestoreRepLon != 0.0) firestoreRepLon else fallbackLon

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
                        Log.d("DeliveryTracking", "Estado es 'entregado', deteniendo actualizaciones")
                        stopLocationUpdates()
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
                Log.e("DeliveryTracking", "Error al obtener ruta", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.d("DeliveryTracking", "Intentando iniciar startLocationUpdates. client=${fusedLocationClient != null}, id=$solicitudId")
        
        if (locationCallback != null || fusedLocationClient == null || solicitudId == null) {
            Log.d("DeliveryTracking", "startLocationUpdates cancelado: callback ya existe o faltan datos")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    Log.d("DeliveryTracking", "Nueva ubicación detectada en emulador: ${location.latitude}, ${location.longitude}")
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        updateLocationInFirestore(location.latitude, location.longitude)
                    }
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                request, 
                locationCallback!!, 
                android.os.Looper.getMainLooper()
            )
            Log.d("DeliveryTracking", "requestLocationUpdates ejecutado exitosamente")
        } catch (e: Exception) {
            Log.e("DeliveryTracking", "Fallo al ejecutar requestLocationUpdates", e)
        }
    }

    private fun updateLocationInFirestore(lat: Double, lon: Double) {
        val id = solicitudId ?: return
        Log.d("DeliveryTracking", "Actualizando Firestore para solicitudId: $id con lat=$lat, lon=$lon")
        
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
                Log.e("DeliveryTracking", "Error al actualizar Firestore", e)
            }
    }

    fun actualizarEstado(nuevoEstado: String) {
        val id = solicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                if (nuevoEstado == "en_camino") {
                    notificationHelper.showNotification("Repartidor en camino", "El repartidor va hacia el cliente.")
                }
            }
    }

    fun finalizarPedido(onFinished: () -> Unit) {
        val id = solicitudId ?: return
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
            locationCallback = null
            Log.d("DeliveryTracking", "Callback removido y actualizaciones detenidas")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
