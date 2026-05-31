package com.example.pagaapp.ui.screens.tracking

import com.google.android.gms.maps.model.LatLng

data class TrackingUiState(
    val userLocation: LatLng = LatLng(4.6097, -74.0817), // Bogotá por defecto
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val repartidoresActivos: List<RepartidorActivo> = emptyList(),
    val selectedPlace: NearbyPlace? = null,
    val isLoading: Boolean = false
)

data class RepartidorActivo(
    val uid: String = "",
    val nombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = ""
)

data class NearbyPlace(
    val id: String,
    val name: String,
    val type: PlaceType,
    val location: LatLng,
    val distanceText: String = ""
)

enum class PlaceType {
    ATM, CORRESPONDENT
}
