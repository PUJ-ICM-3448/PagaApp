package com.example.pagaapp.ui.screens.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = viewModel(),
    onBack: () -> Unit = {},
    showBack: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // API KEY dinámica del Manifest
    val googleApiKey = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) { "" }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6280, -74.0649), 15f)
    }

    // Centrar al cambiar ubicación (solo la primera vez para no molestar al usuario)
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // Centrar al seleccionar lugar
    LaunchedEffect(uiState.selectedPlace) {
        uiState.selectedPlace?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it.location, 16f))
        }
    }

    // Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startLocationUpdates(fusedLocationClient, viewModel)
        } else {
            viewModel.updateUserLocation(null)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            startLocationUpdates(fusedLocationClient, viewModel)
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            title = {
                Column {
                    Text("Nearby Cash Points", color = Color(0xFF1E293B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Bogotá Demo Mode", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3B82F6))
                }
            },
            navigationIcon = {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        )

        // MAPA FIJO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            ) {
                // Ubicación Actual como PUNTO AZUL (Circle)
                uiState.userLocation?.let { pos ->
                    Circle(
                        center = pos,
                        radius = 45.0,
                        fillColor = Color(0x332563EB),
                        strokeColor = Color(0x662563EB),
                        strokeWidth = 2f
                    )
                    Circle(
                        center = pos,
                        radius = 12.0,
                        fillColor = Color(0xFF2563EB),
                        strokeColor = Color.White,
                        strokeWidth = 6f
                    )
                }

                // Marcadores de Lugares
                uiState.nearbyPlaces.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.location),
                        title = place.name,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when(place.type) {
                                PlaceType.ATM -> BitmapDescriptorFactory.HUE_RED
                                PlaceType.CORRESPONDENT -> BitmapDescriptorFactory.HUE_ORANGE
                                PlaceType.PARTNER -> BitmapDescriptorFactory.HUE_VIOLET
                                PlaceType.STORE -> BitmapDescriptorFactory.HUE_GREEN
                            }
                        ),
                        onClick = {
                            viewModel.selectPlace(place)
                            uiState.userLocation?.let { user ->
                                viewModel.calculateRoute(user, place.location, googleApiKey)
                            }
                            false
                        }
                    )
                }

                // Ruta
                if (uiState.routePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePoints,
                        color = Color(0xFF2563EB),
                        width = 12f,
                        jointType = com.google.android.gms.maps.model.JointType.ROUND
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Locations Near You", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            if (uiState.isCalculatingRoute) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.nearbyPlaces) { place ->
                val isSelected = uiState.selectedPlace?.id == place.id
                NearbyPlaceItem(
                    place = place,
                    isSelected = isSelected,
                    onSelect = {
                        viewModel.selectPlace(place)
                        uiState.userLocation?.let { user ->
                            viewModel.calculateRoute(user, place.location, googleApiKey)
                        }
                    },
                    onExternalMapsClick = {
                        val uri = Uri.parse("google.navigation:q=${place.location.latitude},${place.location.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun NearbyPlaceItem(
    place: NearbyPlace,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onExternalMapsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getPlaceColor(place.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = getPlaceColor(place.type), modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                Text(place.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(place.distanceText, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 12.sp)
                Row {
                    IconButton(onClick = onSelect, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Directions, contentDescription = "Ruta", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onExternalMapsClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Navigation, contentDescription = "Maps", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun getPlaceColor(type: PlaceType): Color = when(type) {
    PlaceType.ATM -> Color(0xFFEF4444)
    PlaceType.CORRESPONDENT -> Color(0xFFF59E0B)
    PlaceType.PARTNER -> Color(0xFF8B5CF6)
    PlaceType.STORE -> Color(0xFF10B981)
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(client: FusedLocationProviderClient, viewModel: TrackingViewModel) {
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
    client.requestLocationUpdates(request, object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { 
                viewModel.updateUserLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }, Looper.getMainLooper())
}
