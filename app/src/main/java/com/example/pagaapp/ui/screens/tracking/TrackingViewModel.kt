package com.example.pagaapp.ui.screens.tracking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import kotlin.math.*

interface DirectionsService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String
    ): DirectionsResponse
}

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String,
    val error_message: String? = null
)

data class Route(
    val overview_polyline: PolylineData
)

data class PolylineData(
    val points: String
)

class TrackingViewModel : ViewModel() {

    private val TAG = "TrackingViewModel"
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private val directionsService: DirectionsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsService::class.java)
    }

    // Ubicación de demo: Universidad Javeriana, Bogotá
    private val bogotaFallback = LatLng(4.6280, -74.0649)

    fun updateUserLocation(location: LatLng?) {
        val rawLocation = location ?: bogotaFallback
        
        // Lógica de "Ubicación Efectiva": Si está a más de 50km de Bogotá, usar fallback
        val distanceToBogota = calculateDistance(rawLocation, bogotaFallback)
        val effectiveLocation = if (distanceToBogota > 50.0) {
            Log.w(TAG, "Ubicación detectada muy lejos de Bogotá (dist: ${distanceToBogota}km). Usando Fallback Javeriana.")
            bogotaFallback
        } else {
            rawLocation
        }

        val isFirstLocation = _uiState.value.userLocation == null
        _uiState.update { it.copy(userLocation = effectiveLocation, isLoading = false) }
        
        if (isFirstLocation) {
            loadNearbyPlaces(effectiveLocation)
        }
    }

    private fun loadNearbyPlaces(effectiveLoc: LatLng) {
        _uiState.update { it.copy(isLoading = true) }
        
        // Generar cajeros cerca de la ubicación efectiva en Bogotá
        val mockPlaces = listOf(
            NearbyPlace("1", "ATM Bancolombia Javeriana", PlaceType.ATM, "Cl. 40 #7-30", LatLng(effectiveLoc.latitude + 0.0008, effectiveLoc.longitude + 0.0009)),
            NearbyPlace("2", "Corresponsal Nequi Calle 45", PlaceType.CORRESPONDENT, "Kr 13 #44-10", LatLng(effectiveLoc.latitude + 0.0030, effectiveLoc.longitude + 0.0019)),
            NearbyPlace("3", "Davivienda Park Way", PlaceType.ATM, "Cl. 39 #24-15", LatLng(effectiveLoc.latitude + 0.0115, effectiveLoc.longitude - 0.0135)),
            NearbyPlace("4", "Paga Todo Teusaquillo", PlaceType.PARTNER, "Kr 19 #34-12", LatLng(effectiveLoc.latitude - 0.0045, effectiveLoc.longitude - 0.0062)),
            NearbyPlace("5", "Tienda Ara Aliada", PlaceType.STORE, "Cl. 45 #13-45", LatLng(effectiveLoc.latitude + 0.0015, effectiveLoc.longitude - 0.0015)),
            NearbyPlace("6", "Cajero BBVA Carrera 7", PlaceType.ATM, "Kr 7 #43-00", LatLng(effectiveLoc.latitude + 0.0025, effectiveLoc.longitude + 0.0029))
        ).map { place ->
            val dist = calculateDistance(effectiveLoc, place.location)
            val distText = if (dist < 1.0) {
                String.format(Locale.getDefault(), "%.0f m", dist * 1000)
            } else {
                String.format(Locale.getDefault(), "%.1f km", dist)
            }
            place.copy(distanceText = distText)
        }.sortedBy { calculateDistance(effectiveLoc, it.location) }

        _uiState.update { it.copy(nearbyPlaces = mockPlaces, isLoading = false) }
    }

    fun selectPlace(place: NearbyPlace) {
        _uiState.update { it.copy(selectedPlace = place) }
    }

    fun calculateRoute(origin: LatLng, destination: LatLng, apiKey: String) {
        if (apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            Log.e(TAG, "Directions API: API Key no configurada o vacía.")
            _uiState.update { it.copy(
                routePoints = listOf(origin, destination),
                errorMessage = "API key no configurada (Ruta directa)"
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingRoute = true, errorMessage = null) }
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destStr = "${destination.latitude},${destination.longitude}"
                val response = directionsService.getDirections(originStr, destStr, apiKey = apiKey)

                Log.d(TAG, "Directions API Status: ${response.status}")
                
                if (response.status == "OK" && response.routes.isNotEmpty()) {
                    val encodedPolyline = response.routes[0].overview_polyline.points
                    val decodedPath = PolyUtil.decode(encodedPolyline)
                    Log.d(TAG, "Directions API: ${decodedPath.size} puntos decodificados.")
                    _uiState.update { it.copy(routePoints = decodedPath, isCalculatingRoute = false) }
                } else {
                    Log.w(TAG, "Directions API falló: ${response.status}. Usando fallback recto.")
                    _uiState.update { it.copy(
                        routePoints = listOf(origin, destination),
                        errorMessage = "Ruta directa (${response.status})",
                        isCalculatingRoute = false
                    ) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en calculateRoute: ${e.message}")
                _uiState.update { it.copy(
                    routePoints = listOf(origin, destination),
                    errorMessage = "Error de red (Ruta directa)",
                    isCalculatingRoute = false
                ) }
            }
        }
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
