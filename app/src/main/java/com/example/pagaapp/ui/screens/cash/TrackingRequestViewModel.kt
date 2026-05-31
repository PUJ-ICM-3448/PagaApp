package com.example.pagaapp.ui.screens.cash

import androidx.lifecycle.ViewModel
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
                    val estado = snapshot.getString("estado") ?: "pendiente"
                    
                    val clienteLat = snapshot.getDouble("clienteLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val clienteLon = snapshot.getDouble("clienteLongitud").let { if (it == null || it == 0.0) defaultLon else it }
                    
                    val repartidorLat = snapshot.getDouble("repartidorLatitud").let { if (it == null || it == 0.0) defaultLat else it }
                    val repartidorLon = snapshot.getDouble("repartidorLongitud").let { if (it == null || it == 0.0) defaultLon else it }

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
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
