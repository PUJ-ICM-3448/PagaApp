package com.example.pagaapp.ui.screens.tracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagaapp.ui.theme.PrimaryGreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    onBack: () -> Unit,
    showBack: Boolean = false,
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.userLocation, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Cash Points", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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

                // Marcadores de Repartidores Activos
                uiState.repartidoresActivos.forEach { repartidor ->
                    Marker(
                        state = MarkerState(position = LatLng(repartidor.latitud, repartidor.longitud)),
                        title = repartidor.nombre,
                        snippet = "Repartidor Activo",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
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
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = place.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Distancia: ${place.distanceText}", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { openGoogleMaps(context, place.location) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Cómo llegar", color = Color.White)
                        }
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
