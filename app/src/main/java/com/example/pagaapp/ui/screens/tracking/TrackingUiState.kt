package com.example.pagaapp.ui.screens.tracking

import com.google.android.gms.maps.model.LatLng

data class TrackingUiState(
    val userLocation: LatLng? = null,
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val selectedPlace: NearbyPlace? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isLoading: Boolean = false,
    val isCalculatingRoute: Boolean = false,
    val errorMessage: String? = null
)

data class NearbyPlace(
    val id: String,
    val name: String,
    val type: PlaceType,
    val address: String,
    val location: LatLng,
    val distanceText: String = ""
)

enum class PlaceType {
    ATM, CORRESPONDENT, STORE, PARTNER
}
