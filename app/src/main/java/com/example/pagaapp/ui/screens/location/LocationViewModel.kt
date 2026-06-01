package com.example.pagaapp.ui.screens.location

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        db.collection("locations").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            
            val locations = snapshot.documents.map { doc ->
                LocationModel(
                    name = doc.getString("name") ?: "Punto de efectivo",
                    typeLabel = doc.getString("type") ?: "ATM",
                    address = doc.getString("address") ?: "Bogotá",
                    distance = "Cerca de ti", // Podría calcularse con la ubicación real
                    type = when(doc.getString("type")) {
                        "ATM" -> LocationType.ATM
                        "BANK" -> LocationType.BANK
                        "STORE" -> LocationType.STORE
                        else -> LocationType.PARTNER
                    }
                )
            }
            _uiState.update { it.copy(locations = locations) }
        }
    }
}
