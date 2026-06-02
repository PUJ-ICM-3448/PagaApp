package com.example.pagaapp.ui.screens.cash

import androidx.lifecycle.ViewModel
import com.example.pagaapp.utils.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackingRequestViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(TrackingRequestUiState())
    val uiState: StateFlow<TrackingRequestUiState> = _uiState.asStateFlow()

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

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
                    val nuevoEstado = snapshot.getString("estado") ?: "pendiente"
                    val estadoAnterior = _uiState.value.estado
                    
                    val clienteLat = snapshot.getDouble("clienteLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val clienteLon = snapshot.getDouble("clienteLongitud").let { if (it == null || it == 0.0) defaultLon else it }
                    
                    val repartidorLat = snapshot.getDouble("repartidorLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val repartidorLon = snapshot.getDouble("repartidorLongitud").let { if (it == null || it == 0.0) defaultLon else it }

                    val repartidorNombre = snapshot.getString("repartidorNombre") ?: ""
                    val monto = snapshot.getString("monto") ?: ""

                    // Persona 3: Notificaciones basadas en cambio de estado
                    if (nuevoEstado != estadoAnterior) {
                        enviarNotificacionEstado(nuevoEstado, repartidorNombre)
                    }

                    _uiState.update {
                        it.copy(
                            clienteLatitud = clienteLat,
                            clienteLongitud = clienteLon,
                            repartidorLatitud = repartidorLat,
                            repartidorLongitud = repartidorLon,
                            estado = nuevoEstado,
                            repartidorNombre = repartidorNombre,
                            monto = monto,
                            isLoading = false
                        )
                    }
                }
            }
    }

    private fun enviarNotificacionEstado(estado: String, repartidor: String) {
        val (titulo, mensaje) = when (estado) {
            "aceptado" -> "¡Pedido Aceptado!" to "El repartidor $repartidor ha aceptado tu solicitud."
            "en_camino" -> "Pedido en Camino" to "$repartidor va en camino a tu ubicación."
            "entregado" -> "Pedido Entregado" to "¡Tu efectivo ha sido entregado con éxito!"
            else -> return
        }
        NotificationHelper.addNotification(titulo, mensaje)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
