package com.example.pagaapp.ui.screens.tracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.userLocation, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { viewModel.selectPlace(null) }
        ) {
            // Marcador del Usuario
            Marker(
                state = MarkerState(position = uiState.userLocation),
                title = "Tu ubicación"
            )

            // Marcadores de Lugares Cercanos
            uiState.nearbyPlaces.forEach { place ->
                Marker(
                    state = MarkerState(position = place.location),
                    title = place.name,
                    snippet = place.distanceText,
                    onClick = {
                        viewModel.selectPlace(place)
                        false
                    }
                )
            }
        }

        // Panel de información del lugar seleccionado
        uiState.selectedPlace?.let { place ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = place.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Distancia: ${place.distanceText}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { openGoogleMaps(context, place.location) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cómo llegar")
                    }
                }
            }
        }
    }
}

private fun openGoogleMaps(context: Context, destination: LatLng) {
    val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}
