package com.example.pagaapp.ui.screens.location

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        _uiState.value = LocationUiState(
            locations = listOf(
                LocationModel(
                    name = "ATM Bancolombia",
                    typeLabel = "ATM",
                    address = "Cra 15 #45-23",
                    distance = "250m",
                    type = LocationType.ATM
                ),
                LocationModel(
                    name = "Corresponsal Efecty",
                    typeLabel = "Bank Correspondent",
                    address = "Cl 47 #16-08",
                    distance = "400m",
                    type = LocationType.BANK
                ),
                LocationModel(
                    name = "Tienda Don Pepe",
                    typeLabel = "Partner Store",
                    address = "Cra 14 #44-15",
                    distance = "180m",
                    type = LocationType.STORE
                ),
                LocationModel(
                    name = "Punto Aliado Nequi",
                    typeLabel = "Cash Partner",
                    address = "Cl 48 #17-32",
                    distance = "550m",
                    type = LocationType.PARTNER
                ),
                LocationModel(
                    name = "ATM Davivienda",
                    typeLabel = "ATM",
                    address = "Cra 16 #46-09",
                    distance = "320m",
                    type = LocationType.ATM
                )
            )
        )
    }
}