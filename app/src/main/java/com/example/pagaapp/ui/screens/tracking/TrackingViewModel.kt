package com.example.pagaapp.ui.screens.tracking

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.*

class TrackingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        loadNearbyPlaces()
        escucharRepartidores()
    }

    private fun escucharRepartidores() {
        db.collection("repartidoresActivos")
            .whereEqualTo("estado", "activo")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RepartidorActivo::class.java)?.copy(uid = doc.id)
                } ?: emptyList()
                
                _uiState.update { it.copy(repartidoresActivos = lista) }
            }
    }

    private fun loadNearbyPlaces() {
        db.collection("locations").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            
            val userLoc = _uiState.value.userLocation
            val places = snapshot.documents.mapNotNull { doc ->
                val lat = doc.getDouble("latitud") ?: return@mapNotNull null
                val lon = doc.getDouble("longitud") ?: return@mapNotNull null
                val name = doc.getString("name") ?: "Punto de efectivo"
                val typeStr = doc.getString("type") ?: "ATM"
                
                val location = LatLng(lat, lon)
                val distance = calculateDistance(userLoc, location)
                
                NearbyPlace(
                    id = doc.id,
                    name = name,
                    type = if (typeStr == "ATM") PlaceType.ATM else PlaceType.CORRESPONDENT,
                    location = location,
                    distanceText = String.format("%.2f km", distance)
                )
            }
            _uiState.update { it.copy(nearbyPlaces = places) }
        }
    }

    fun selectPlace(place: NearbyPlace?) {
        _uiState.update { it.copy(selectedPlace = place) }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val radius = 6371.0
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c
    }
}
