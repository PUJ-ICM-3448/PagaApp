package com.example.pagaapp.ui.screens.cash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagaapp.data.network.DirectionsRetrofitClient
import com.example.pagaapp.util.NotificationHelper
import com.example.pagaapp.util.decodePolyline
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackingRequestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val notificationHelper = NotificationHelper(application)
    
    private val _uiState = MutableStateFlow(TrackingRequestUiState())
    val uiState: StateFlow<TrackingRequestUiState> = _uiState.asStateFlow()

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var lastNotifiedStatus: String? = null

    private val GOOGLE_MAPS_API_KEY = "AIzaSyAPXZetb9acvDrOrGcOUUOiCdir59np-Cw"

    // Coordenadas de demo: Universidad Javeriana Bogotá
    private val defaultLat = 4.6280
    private val defaultLon = -74.0647

    fun startTracking(solicitudId: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        listenerRegistration = db.collection("solicitudesEfectivo").document(solicitudId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val estado = snapshot.getString("estado") ?: "pendiente"
                    
                    // Notificar al cliente si el estado cambió
                    if (lastNotifiedStatus != null && estado != lastNotifiedStatus) {
                        when (estado) {
                            "aceptado" -> notificationHelper.showNotification("Pedido aceptado", "El repartidor aceptó la solicitud.")
                            "en_camino" -> notificationHelper.showNotification("Repartidor en camino", "El repartidor va hacia tu ubicación.")
                            "entregado" -> notificationHelper.showNotification("Pedido entregado", "La entrega fue finalizada correctamente.")
                        }
                    }
                    lastNotifiedStatus = estado

                    val clienteLat = snapshot.getDouble("clienteLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val clienteLon = snapshot.getDouble("clienteLongitud").let { if (it == null || it == 0.0) defaultLon else it }

                    val repartidorLat = snapshot.getDouble("repartidorLatitud") ?: 0.0
                    val repartidorLon = snapshot.getDouble("repartidorLongitud") ?: 0.0

                    val repartidorNombre = snapshot.getString("repartidorNombre") ?: ""
                    val monto = snapshot.getString("monto") ?: ""

                    _uiState.update {
                        it.copy(
                            clienteLatitud = clienteLat,
                            clienteLongitud = clienteLon,
                            repartidorLatitud = repartidorLat,
                            repartidorLongitud = repartidorLon,
                            estado = estado,
                            repartidorNombre = repartidorNombre,
                            monto = monto,
                            isLoading = false
                        )
                    }

                    // Fetch real route if delivery is active
                    if ((estado == "aceptado" || estado == "en_camino")
                        && repartidorLat != 0.0 && repartidorLon != 0.0) {
                        fetchRoute(repartidorLat, repartidorLon, clienteLat, clienteLon)
                    }
                }
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
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
