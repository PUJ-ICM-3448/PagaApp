package com.example.pagaapp.ui.screens.location

data class LocationUiState(
    val locations: List<LocationModel> = emptyList(),
    val isLoading: Boolean = false
)