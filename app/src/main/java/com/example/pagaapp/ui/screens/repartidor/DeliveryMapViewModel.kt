package com.example.pagaapp.ui.screens.repartidor

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.pagaapp.utils.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DeliveryMapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val _uiState = MutableStateFlow(DeliveryMapUiState())
    val uiState: StateFlow<DeliveryMapUiState> = _uiState.asStateFlow()

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentSolicitudId: String? = null

    fun setLocationClient(client: FusedLocationProviderClient) {
        fusedLocationClient = client
    }

    fun setEvidenceUri(uri: Uri?) {
        _uiState.update { it.copy(evidenceUri = uri) }
    }

    fun startTracking(solicitudId: String) {
        currentSolicitudId = solicitudId
        
        obtenerUbicacionActual()

        listenerRegistration = db.collection("solicitudesEfectivo").document(solicitudId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val estado = snapshot.getString("estado") ?: ""
                val clienteLat = snapshot.getDouble("clienteLatitud") ?: 0.0
                val clienteLon = snapshot.getDouble("clienteLongitud") ?: 0.0
                val nombreCliente = snapshot.getString("clienteNombre") ?: "Cliente"
                val monto = snapshot.getString("monto") ?: ""
                val repLat = snapshot.getDouble("repartidorLatitud") ?: 0.0
                val repLon = snapshot.getDouble("repartidorLongitud") ?: 0.0

                _uiState.update {
                    it.copy(
                        estado = estado,
                        clienteLatitud = clienteLat,
                        clienteLongitud = clienteLon,
                        clienteNombre = nombreCliente,
                        monto = monto,
                        repartidorLatitud = if (it.repartidorLatitud == 0.0) repLat else it.repartidorLatitud,
                        repartidorLongitud = if (it.repartidorLongitud == 0.0) repLon else it.repartidorLongitud,
                        isLoading = false
                    )
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            ?.addOnSuccessListener { location ->
                if (location != null) {
                    _uiState.update {
                        it.copy(
                            repartidorLatitud = location.latitude,
                            repartidorLongitud = location.longitude
                        )
                    }
                    actualizarUbicacionEnFirebase(location.latitude, location.longitude)
                }
            }
    }

    private fun actualizarUbicacionEnFirebase(lat: Double, lon: Double) {
        val id = currentSolicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update(
                "repartidorLatitud", lat,
                "repartidorLongitud", lon
            )
    }

    fun actualizarEstado(nuevoEstado: String) {
        val id = currentSolicitudId ?: return
        db.collection("solicitudesEfectivo").document(id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                val mensaje = when(nuevoEstado) {
                    "en_camino" -> "Has iniciado la entrega. ¡Conduce con cuidado!"
                    "entregado" -> "Entrega finalizada con éxito."
                    else -> "Estado actualizado a $nuevoEstado"
                }
                NotificationHelper.addNotification("Actualización de Entrega", mensaje)
            }
    }

    fun finalizarPedidoConEvidencia(onSuccess: () -> Unit) {
        val id = currentSolicitudId ?: return
        val uri = _uiState.value.evidenceUri

        if (uri == null) {
            _uiState.update { it.copy(error = "Se requiere una foto como evidencia de entrega") }
            return
        }

        _uiState.update { it.copy(isUploading = true) }

        val storageRef = storage.reference.child("evidencias_entrega/${id}_${UUID.randomUUID()}.jpg")
        
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("solicitudesEfectivo").document(id)
                        .update(
                            "estado", "entregado",
                            "evidenciaUrl", downloadUrl.toString(),
                            "fechaEntrega", System.currentTimeMillis()
                        )
                        .addOnSuccessListener {
                            _uiState.update { it.copy(isUploading = false) }
                            NotificationHelper.addNotification("Entrega Finalizada", "Has entregado el efectivo y subido la evidencia.")
                            onSuccess()
                        }
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isUploading = false, error = "Error al subir evidencia: ${e.message}") }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
