package com.example.pagaapp.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng

object DemoDataSeeder {
    private val db = FirebaseFirestore.getInstance()

    fun seedDemoData() {
        seedLocations()
    }

    private fun seedLocations() {
        val locationsRef = db.collection("locations")
        
        // Solo agregar si está vacía para no duplicar
        locationsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val demoLocations = listOf(
                    mapOf(
                        "name" to "Cajero Bancolombia - Javeriana",
                        "type" to "ATM",
                        "address" to "Carrera 7 # 40-62",
                        "latitud" to 4.6285,
                        "longitud" to -74.0645
                    ),
                    mapOf(
                        "name" to "Corresponsal Nequi - Oxxo",
                        "type" to "PARTNER",
                        "address" to "Calle 45 # 7-10",
                        "latitud" to 4.6310,
                        "longitud" to -74.0630
                    ),
                    mapOf(
                        "name" to "ATM Davivienda - Hospital San Ignacio",
                        "type" to "ATM",
                        "address" to "Carrera 7 # 40-10",
                        "latitud" to 4.6275,
                        "longitud" to -74.0655
                    )
                )

                for (loc in demoLocations) {
                    locationsRef.add(loc)
                }
            }
        }
    }
}
