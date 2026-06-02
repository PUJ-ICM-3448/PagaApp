package com.example.pagaapp.ui.screens.cash

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingRequestScreen(
    solicitudId: String,
    navController: NavController,
    viewModel: TrackingRequestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(solicitudId) {
        viewModel.startTracking(solicitudId)
    }

    // Persona 3: Alertar al usuario cuando el estado cambie
    LaunchedEffect(uiState.estado) {
        val mensaje = when (uiState.estado) {
            "aceptado" -> "¡Un repartidor ha aceptado tu pedido!"
            "en_camino" -> "El repartidor ya va en camino."
            "entregado" -> "¡Pedido entregado con éxito!"
            else -> null
        }
        mensaje?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido", color = Color.White, fontWeight = FontWeight.Bold) },
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
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val clienteLocation = LatLng(uiState.clienteLatitud, uiState.clienteLongitud)
            val repartidorLocation = LatLng(uiState.repartidorLatitud, uiState.repartidorLongitud)
            
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(clienteLocation, 15f)
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = rememberMarkerState(position = clienteLocation),
                        title = "Tu ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    if (uiState.estado == "aceptado" || uiState.estado == "en_camino") {
                        Marker(
                            state = rememberMarkerState(position = repartidorLocation),
                            title = "Repartidor: ${uiState.repartidorNombre}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }

                // Panel flotante con info del pedido y ESTADOS CLAROS
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val estadoLabel = when (uiState.estado) {
                                    "pendiente" -> "Esperando repartidor"
                                    "aceptado" -> "Repartidor asignado"
                                    "en_camino" -> "En camino"
                                    "entregado" -> "Entregado"
                                    else -> "Estado: ${uiState.estado}"
                                }
                                
                                Text(
                                    text = estadoLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (uiState.estado == "entregado") PrimaryGreen else Color(0xFF1E293B)
                                )
                                Text("Pedido por: $${uiState.monto}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            }
                            
                            if (uiState.estado == "en_camino") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 4.dp,
                                    color = PrimaryGreen
                                )
                            }
                        }
                        
                        if (uiState.repartidorNombre.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(uiState.repartidorNombre.take(1), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Repartidor asignado", fontSize = 12.sp, color = TextSecondary)
                                    Text(uiState.repartidorNombre, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
