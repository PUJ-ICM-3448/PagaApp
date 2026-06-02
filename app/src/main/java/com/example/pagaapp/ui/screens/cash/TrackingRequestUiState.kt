package com.example.pagaapp.ui.screens.cash

import com.google.android.gms.maps.model.LatLng

data class TrackingRequestUiState(
    val clienteLatitud: Double = 0.0,
    val clienteLongitud: Double = 0.0,
    val repartidorLatitud: Double = 0.0,
    val repartidorLongitud: Double = 0.0,
    val estado: String = "pendiente",
    val repartidorNombre: String = "",
    val monto: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val routePoints: List<LatLng> = emptyList()
)
