package com.example.pagaapp.ui.screens.repartidor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.pagaapp.util.NotificationHelper
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

class RepartidorViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notificationHelper = NotificationHelper(application)
    
    private val _uiState = MutableStateFlow(RepartidorUiState())
    val uiState: StateFlow<RepartidorUiState> = _uiState.asStateFlow()

    private var pendingListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var activeListener: com.google.firebase.firestore.ListenerRegistration? = null

    // Para evitar notificar múltiples veces la misma solicitud
    private val notifiedRequests = mutableSetOf<String>()
    private var isFirstLoad = true

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

                // Si es la primera carga, marcamos como notificadas las existentes para no inundar
                if (isFirstLoad) {
                    lista.forEach { notifiedRequests.add(it.id) }
                    isFirstLoad = false
                } else {
                    // Notificar solo las nuevas que entren después
                    lista.forEach { solicitud ->
                        if (!notifiedRequests.contains(solicitud.id)) {
                            notificationHelper.showNotification(
                                "Nueva solicitud de efectivo",
                                "${solicitud.clienteNombre} solicita $${solicitud.monto}"
                            )
                            notifiedRequests.add(solicitud.id)
                        }
                    }
                }

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
                
                // No se escribe ninguna ubicación aquí: la posición real del
                // repartidor la fija el GPS desde DeliveryMapViewModel. Queda en 0.0
                // hasta el primer fix (la UI muestra "Buscando tu ubicación…").
                val updates = hashMapOf<String, Any>(
                    "estado" to "aceptado",
                    "repartidorId" to uid,
                    "repartidorNombre" to nombre,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("solicitudesEfectivo").document(solicitud.id)
                    .update(updates)
                    .addOnSuccessListener {
                        // No se auto-notifica al repartidor. El cliente recibirá la notificación.
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
