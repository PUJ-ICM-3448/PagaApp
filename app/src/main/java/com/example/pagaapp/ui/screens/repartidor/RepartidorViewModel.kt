package com.example.pagaapp.ui.screens.repartidor

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SolicitudEfectivo(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val monto: String = "",
    val clienteLatitud: Double = 0.0,
    val clienteLongitud: Double = 0.0,
    val estado: String = "",
    val repartidorId: String = "",
    val repartidorNombre: String = "",
    val repartidorLatitud: Double = 0.0,
    val repartidorLongitud: Double = 0.0
)

data class RepartidorUiState(
    val solicitudesPendientes: List<SolicitudEfectivo> = emptyList(),
    val solicitudesEnCurso: List<SolicitudEfectivo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RepartidorViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(RepartidorUiState())
    val uiState: StateFlow<RepartidorUiState> = _uiState.asStateFlow()

    private var pendingListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var activeListener: com.google.firebase.firestore.ListenerRegistration? = null

    // Ubicación de fallback para el repartidor: Museo Nacional
    private val fallbackLat = 4.6156
    private val fallbackLon = -74.0690

    init {
        observarSolicitudes()
    }

    private fun observarSolicitudes() {
        val uid = auth.currentUser?.uid ?: return
        
        // Observar pendientes
        pendingListener = db.collection("solicitudesEfectivo")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(error = e.message) }
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SolicitudEfectivo::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _uiState.update { it.copy(solicitudesPendientes = lista) }
            }

        // Observar en curso para este repartidor
        activeListener = db.collection("solicitudesEfectivo")
            .whereEqualTo("repartidorId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SolicitudEfectivo::class.java)?.copy(id = doc.id)
                }?.filter { it.estado == "aceptado" || it.estado == "en_camino" } ?: emptyList()
                _uiState.update { it.copy(solicitudesEnCurso = lista) }
            }
    }

    fun aceptarSolicitud(solicitud: SolicitudEfectivo, onAccepted: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        
        _uiState.update { it.copy(isLoading = true) }

        // Fetch real delivery name from Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val nombre = userDoc.getString("name") ?: currentUser.displayName ?: "Repartidor"
                
                val updates = hashMapOf<String, Any>(
                    "estado" to "aceptado",
                    "repartidorId" to uid,
                    "repartidorNombre" to nombre,
                    "repartidorLatitud" to fallbackLat,
                    "repartidorLongitud" to fallbackLon,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("solicitudesEfectivo").document(solicitud.id)
                    .update(updates)
                    .addOnSuccessListener {
                        _uiState.update { it.copy(isLoading = false) }
                        onAccepted(solicitud.id)
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = "Error al obtener perfil") }
            }
    }

    fun actualizarEstado(solicitudId: String, nuevoEstado: String) {
        db.collection("solicitudesEfectivo").document(solicitudId)
            .update("estado", nuevoEstado)
    }

    override fun onCleared() {
        super.onCleared()
        pendingListener?.remove()
        activeListener?.remove()
    }
}
