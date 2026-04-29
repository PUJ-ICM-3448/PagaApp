package com.example.pagaapp.ui.screens.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
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
    onBack: () -> Unit = {},
    showBack: Boolean = false,
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Leer la API Key real desde el AndroidManifest meta-data
    val googleApiKey = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6285, -74.0648), 15f)
    }

    // Centrar cámara cuando cambia la ubicación del usuario o el lugar seleccionado
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    LaunchedEffect(uiState.selectedPlace) {
        uiState.selectedPlace?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it.location, 16f))
        }
    }

    // Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            startLocationUpdates(fusedLocationClient, viewModel)
        } else {
            viewModel.updateUserLocation(null) // Carga ubicación fallback
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
            .background(Color(0xFFF3F4F6))
    ) {
        // --- HEADER ---
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            title = {
                Column {
                    Text("Nearby Cash Points", color = Color(0xFF0D1B3D), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Find locations near you", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7C8799))
                }
            },
            navigationIcon = {
                if (showBack) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F3F5),
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF374151)
                            )
                        }
                    }
                }
            },
            actions = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.padding(end = 12.dp).size(40.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        )

        // --- MAP SECTION ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                // User Marker
                uiState.userLocation?.let { pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Tu ubicación",
                        rotation = uiState.userHeading,
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        icon = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_compass)
                    )
                }

                // Places Markers
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

                // Polyline
                if (uiState.routePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePoints,
                        color = Color(0xFF16A34A),
                        width = 10f
                    )
                }
            }
        }

        // --- LIST SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nearby Locations", style = MaterialTheme.typography.titleMedium, color = Color(0xFF0D1B3D), fontWeight = FontWeight.Bold)
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "${uiState.nearbyPlaces.size} found",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color(0xFF16A34A),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp) 
        ) {
            items(uiState.nearbyPlaces) { place ->
                NearbyPlaceCard(
                    place = place,
                    isSelected = uiState.selectedPlace?.id == place.id,
                    onSelect = {
                        viewModel.selectPlace(place)
                        uiState.userLocation?.let { user ->
                            viewModel.calculateRoute(user, place.location, googleApiKey)
                        }
                    },
                    onDirectionsClick = {
                        // Abrir Google Maps con navegación al lugar seleccionado
                        openGoogleMapsNavigation(context, place, uiState.userLocation)
                    }
                )
            }
        }
    }
}

@Composable
fun NearbyPlaceCard(
    place: NearbyPlace,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getPlaceColor(place.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = getPlaceColor(place.type),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B3D), fontSize = 16.sp)
                Text(place.type.name, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7C8799))
                Text(place.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7C8799))
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = Color(0xFFF1F3F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        place.distanceText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDirectionsClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Navigation, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp), 
                        tint = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Get directions", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
           ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(client: FusedLocationProviderClient, viewModel: TrackingViewModel) {
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
    client.requestLocationUpdates(request, object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { 
                viewModel.updateUserLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }, Looper.getMainLooper())
}

/**
 * Abre Google Maps con navegación hacia el lugar seleccionado.
 * Si el usuario tiene ubicación conocida, la usa como origen para mostrar la ruta completa.
 * Si Google Maps no está instalado, abre la ruta en el navegador como fallback.
 */
private fun openGoogleMapsNavigation(context: Context, place: NearbyPlace, userLocation: LatLng?) {
    val destLat = place.location.latitude
    val destLng = place.location.longitude

    // Intentar abrir Google Maps con navegación (modo driving)
    val gmmUri = if (userLocation != null) {
        // Con origen y destino — muestra ruta completa por calles
        Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
            "&origin=${userLocation.latitude},${userLocation.longitude}" +
            "&destination=$destLat,$destLng" +
            "&travelmode=driving"
        )
    } else {
        // Solo destino — Google Maps usará la ubicación actual del dispositivo
        Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
    }

    val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri)

    // Intentar abrir con Google Maps primero
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Fallback: abrir en el navegador
        mapIntent.setPackage(null)
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la navegación", Toast.LENGTH_SHORT).show()
        }
    }
}
