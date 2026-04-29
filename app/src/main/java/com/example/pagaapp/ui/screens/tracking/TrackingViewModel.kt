package com.example.pagaapp.ui.screens.tracking

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
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

// Retrofit interface for Google Directions API
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
    val status: String
)

data class Route(
    val overview_polyline: PolylineData
)

data class PolylineData(
    val points: String
)

class TrackingViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        startCompass()
    }

    private fun startCompass() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            
            // Convert azimuth to degrees
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            _uiState.update { it.copy(userHeading = azimuth) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    private val directionsService: DirectionsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsService::class.java)
    }

    // Default fallback location (Javeriana, Bogota) as requested
    private val javerianaLocation = LatLng(4.6280, -74.0649)

    fun updateUserLocation(location: LatLng?) {
        val finalLocation = location ?: javerianaLocation
        val isFirstLocation = _uiState.value.userLocation == null
        
        _uiState.update { it.copy(userLocation = finalLocation, isLoading = false) }
        
        // Cargar cajeros alrededor de la ubicación inicial (real o fallback en Bogotá)
        if (isFirstLocation) {
            loadNearbyPlaces(finalLocation)
        }
    }

    private fun loadNearbyPlaces(userLoc: LatLng) {
        _uiState.update { it.copy(isLoading = true) }
        
        // Generamos puntos de interés simulados alrededor de la ubicación en Bogotá
        val mockPlaces = listOf(
            NearbyPlace(
                "1", "Cajero Bancolombia - Javeriana", PlaceType.ATM,
                "Calle 40 # 7-30", LatLng(userLoc.latitude + 0.0012, userLoc.longitude + 0.0008)
            ),
            NearbyPlace(
                "2", "Corresponsal Nequi - Calle 45", PlaceType.CORRESPONDENT,
                "Carrera 13 # 44-10", LatLng(userLoc.latitude - 0.0018, userLoc.longitude + 0.0022)
            ),
            NearbyPlace(
                "3", "ATM Davivienda Galerías", PlaceType.ATM,
                "Calle 53 # 25-05", LatLng(userLoc.latitude + 0.0035, userLoc.longitude - 0.0045)
            ),
            NearbyPlace(
                "4", "Punto Paga Todo Teusaquillo", PlaceType.PARTNER,
                "Carrera 19 # 34-12", LatLng(userLoc.latitude - 0.0025, userLoc.longitude - 0.0012)
            ),
            NearbyPlace(
                "5", "Tienda Ara Aliada", PlaceType.STORE,
                "Calle 45 # 13-45", LatLng(userLoc.latitude + 0.0015, userLoc.longitude - 0.0025)
            ),
            NearbyPlace(
                "6", "Cajero BBVA Universidad", PlaceType.ATM,
                "Carrera 7 # 43-00", LatLng(userLoc.latitude + 0.0025, userLoc.longitude + 0.0012)
            )
        ).map { place ->
            val distanceKm = calculateDistance(userLoc, place.location)
            val distanceText = if (distanceKm < 1.0) {
                String.format(Locale.getDefault(), "%.0f m", distanceKm * 1000)
            } else {
                String.format(Locale.getDefault(), "%.1f km", distanceKm)
            }
            place.copy(distanceText = distanceText)
        }

        _uiState.update { it.copy(nearbyPlaces = mockPlaces, isLoading = false) }
    }

    fun selectPlace(place: NearbyPlace) {
        _uiState.update { it.copy(selectedPlace = place) }
    }

    fun calculateRoute(origin: LatLng, destination: LatLng, apiKey: String) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GOOGLE_MAPS_API_KEY") {
            // Fallback inmediato a línea recta si no hay API Key válida
            _uiState.update { it.copy(
                routePoints = listOf(origin, destination), 
                isCalculatingRoute = false,
                errorMessage = "API Key no configurada. Mostrando ruta directa."
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingRoute = true, errorMessage = null) }
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destStr = "${destination.latitude},${destination.longitude}"
                val response = directionsService.getDirections(originStr, destStr, apiKey = apiKey)

                if (response.status == "OK" && response.routes.isNotEmpty()) {
                    val encodedPolyline = response.routes[0].overview_polyline.points
                    val decodedPath = PolyUtil.decode(encodedPolyline)
                    _uiState.update { it.copy(routePoints = decodedPath, isCalculatingRoute = false) }
                } else {
                    // Fallback a línea recta si la API devuelve error o no hay rutas
                    _uiState.update { it.copy(
                        routePoints = listOf(origin, destination),
                        errorMessage = "No se pudo obtener ruta por calles (${response.status}). Mostrando ruta directa.",
                        isCalculatingRoute = false
                    ) }
                }
            } catch (e: Exception) {
                // Fallback a línea recta en caso de error de red
                _uiState.update { it.copy(
                    routePoints = listOf(origin, destination),
                    errorMessage = "Error de conexión. Mostrando ruta directa.",
                    isCalculatingRoute = false
                ) }
            }
        }
    }

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
