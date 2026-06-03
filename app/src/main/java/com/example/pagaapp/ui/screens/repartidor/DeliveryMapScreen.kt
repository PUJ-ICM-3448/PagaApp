package com.example.pagaapp.ui.screens.repartidor

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.example.pagaapp.ui.theme.PrimaryGreen
import com.example.pagaapp.ui.theme.AppBackground
import com.example.pagaapp.ui.theme.CardBackground
import com.example.pagaapp.ui.theme.TextPrimary
import com.example.pagaapp.ui.theme.TextSecondary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapScreen(
    solicitudId: String,
    navController: NavController,
    viewModel: DeliveryMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Inicia el tracking (cliente de ubicación + Firestore) una vez tenemos el permiso
    fun iniciarTracking() {
        viewModel.setLocationClient(LocationServices.getFusedLocationProviderClient(context))
        viewModel.startTracking(solicitudId)
    }

    // Lanzador para solicitar el permiso de ubicación en tiempo de ejecución
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            iniciarTracking()
        } else {
            Toast.makeText(
                context,
                "Se necesita permiso de ubicación para mostrar tu posición real en el mapa.",
                Toast.LENGTH_LONG
            ).show()
            // Inicia el tracking igualmente para escuchar Firestore (sin GPS propio)
            viewModel.startTracking(solicitudId)
        }
    }

    LaunchedEffect(Unit) {
        val yaConcedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (yaConcedido) {
            iniciarTracking()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrega en curso", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            val clienteLocation = LatLng(uiState.clienteLatitud, uiState.clienteLongitud)
            val repartidorLocation = LatLng(uiState.repartidorLatitud, uiState.repartidorLongitud)

            val repartidorDisponible = uiState.repartidorLatitud != 0.0 || uiState.repartidorLongitud != 0.0
            val clienteDisponible = uiState.clienteLatitud != 0.0 || uiState.clienteLongitud != 0.0

            // Centro inicial: ubicación del repartidor si ya existe, si no la del cliente
            val centroInicial = if (repartidorDisponible) repartidorLocation else clienteLocation
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(centroInicial, 15f)
            }

            // Actualizar cámara para ajustar ambos puntos si están disponibles
            LaunchedEffect(clienteLocation, repartidorLocation) {
                if (clienteLocation.latitude != 0.0 && repartidorLocation.latitude != 0.0) {
                    try {
                        val bounds = LatLngBounds.builder()
                            .include(clienteLocation)
                            .include(repartidorLocation)
                            .build()
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    if (clienteDisponible) {
                        Marker(
                            state = rememberMarkerState(position = clienteLocation),
                            title = "Cliente: ${uiState.clienteNombre}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }

                    if (repartidorDisponible) {
                        Marker(
                            state = rememberMarkerState(position = repartidorLocation),
                            title = "Tu ubicación",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    if (repartidorDisponible && clienteDisponible &&
                        (uiState.estado == "aceptado" || uiState.estado == "en_camino")) {
                        val points = if (uiState.routePoints.isNotEmpty()) uiState.routePoints 
                                     else listOf(repartidorLocation, clienteLocation)
                        
                        Polyline(
                            points = points,
                            color = PrimaryGreen,
                            width = 12f
                        )
                    }
                }

                // Aviso mientras aún no llega la primera ubicación real del GPS
                if (uiState.repartidorLatitud == 0.0 && uiState.repartidorLongitud == 0.0) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Buscando tu ubicación…", color = TextPrimary)
                        }
                    }
                }

                // Panel inferior con detalles
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Entregar a: ${uiState.clienteNombre}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Monto: $${uiState.monto}",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (uiState.estado == "aceptado") {
                            Button(
                                onClick = { viewModel.actualizarEstado("en_camino") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("MARCAR EN CAMINO", fontWeight = FontWeight.Bold)
                            }
                        } else if (uiState.estado == "en_camino") {
                            Button(
                                onClick = {
                                    viewModel.finalizarPedido {
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Text("FINALIZAR ENTREGA", fontWeight = FontWeight.Bold)
                            }
                        } else if (uiState.estado == "entregado") {
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Pedido ya entregado - Volver")
                            }
                        }
                    }
                }
            }
        }
    }
}
