package com.example.pagaapp.ui.screens.repartidor

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
import com.example.pagaapp.R
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

    LaunchedEffect(Unit) {
        viewModel.setLocationClient(LocationServices.getFusedLocationProviderClient(context))
        viewModel.startTracking(solicitudId)
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
            
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(repartidorLocation, 15f)
            }

            // Actualizar cámara para ajustar ambos puntos si están disponibles
            LaunchedEffect(clienteLocation, repartidorLocation) {
                if (clienteLocation.latitude != 0.0 && repartidorLocation.latitude != 0.0) {
                    val bounds = LatLngBounds.builder()
                        .include(clienteLocation)
                        .include(repartidorLocation)
                        .build()
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = rememberMarkerState(position = clienteLocation),
                        title = "Cliente: ${uiState.clienteNombre}",
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.cliente)
                    )

                    Marker(
                        state = rememberMarkerState(position = repartidorLocation),
                        title = "Tu ubicación",
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.repartidor)
                    )

                    if (uiState.estado == "aceptado" || uiState.estado == "en_camino") {
                        Polyline(
                            points = listOf(repartidorLocation, clienteLocation),
                            color = PrimaryGreen,
                            width = 10f
                        )
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
                                Text("Marcar como EN CAMINO", fontWeight = FontWeight.Bold)
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
