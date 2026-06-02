package com.example.pagaapp.ui.screens.repartidor

import android.net.Uri

data class DeliveryMapUiState(
    val clienteLatitud: Double = 0.0,
    val clienteLongitud: Double = 0.0,
    val clienteNombre: String = "",
    val monto: String = "",
    val repartidorLatitud: Double = 0.0,
    val repartidorLongitud: Double = 0.0,
    val estado: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val evidenceUri: Uri? = null,
    val isUploading: Boolean = false
)
