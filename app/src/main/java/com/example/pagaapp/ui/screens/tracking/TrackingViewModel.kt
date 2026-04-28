package com.example.pagaapp.ui.screens.tracking

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.*

class TrackingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        loadNearbyPlaces()
    }

    private fun loadNearbyPlaces() {
        val userLoc = _uiState.value.userLocation
        
        // Datos simulados (Cajeros y Corresponsales en Bogotá cerca del centro)
        val mockPlaces = listOf(
            NearbyPlace(
                "1", "Cajero Bancolombia", PlaceType.ATM, 
                LatLng(userLoc.latitude + 0.002, userLoc.longitude + 0.002)
            ),
            NearbyPlace(
                "2", "Corresponsal Nequi", PlaceType.CORRESPONDENT, 
                LatLng(userLoc.latitude - 0.003, userLoc.longitude + 0.001)
            ),
            NearbyPlace(
                "3", "Cajero Davivienda", PlaceType.ATM, 
                LatLng(userLoc.latitude + 0.001, userLoc.longitude - 0.004)
            ),
            NearbyPlace(
                "4", "Punto Paga Todo", PlaceType.CORRESPONDENT, 
                LatLng(userLoc.latitude - 0.001, userLoc.longitude - 0.002)
            )
        ).map { place ->
            val distance = calculateDistance(userLoc, place.location)
            place.copy(distanceText = String.format("%.2f km", distance))
        }

        _uiState.update { it.copy(nearbyPlaces = mockPlaces) }
    }

    fun selectPlace(place: NearbyPlace?) {
        _uiState.update { it.copy(selectedPlace = place) }
    }

    // Fórmula de Haversine para calcular distancia en km
    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val radius = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c
    }
}
