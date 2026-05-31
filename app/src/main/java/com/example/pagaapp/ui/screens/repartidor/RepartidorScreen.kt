package com.example.pagaapp.ui.screens.repartidor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.theme.PrimaryGreen
import com.example.pagaapp.ui.theme.AppBackground
import com.example.pagaapp.ui.theme.CardBackground
import com.example.pagaapp.ui.theme.TextPrimary
import com.example.pagaapp.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorScreen(
    navController: NavController,
    viewModel: RepartidorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Panel de Repartidor", 
                        color = Color.White,
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección: Solicitudes en curso
            item {
                Text(
                    "Solicitudes en curso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.solicitudesEnCurso.isEmpty()) {
                item {
                    Text(
                        "No tienes pedidos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.solicitudesEnCurso) { solicitud ->
                    SolicitudEnCursoCard(
                        solicitud = solicitud,
                        onVerMapa = { navController.navigate(Routes.DeliveryMap.createRoute(solicitud.id)) },
                        onEnCamino = { viewModel.actualizarEstado(solicitud.id, "en_camino") },
                        onFinalizar = { viewModel.actualizarEstado(solicitud.id, "entregado") }
                    )
                }
            }

            // Sección: Solicitudes disponibles
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Solicitudes disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.solicitudesPendientes.isEmpty()) {
                item {
                    Text(
                        "No hay solicitudes nuevas en este momento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.solicitudesPendientes) { solicitud ->
                    SolicitudPendienteCard(
                        solicitud = solicitud,
                        onAceptar = {
                            viewModel.aceptarSolicitud(solicitud) { id ->
                                navController.navigate(Routes.DeliveryMap.createRoute(id))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SolicitudPendienteCard(solicitud: SolicitudEfectivo, onAceptar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(solicitud.clienteNombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Monto: $${solicitud.monto}", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        solicitud.estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAceptar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Aceptar pedido", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SolicitudEnCursoCard(
    solicitud: SolicitudEfectivo,
    onVerMapa: () -> Unit,
    onEnCamino: () -> Unit,
    onFinalizar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(solicitud.clienteNombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Monto: $${solicitud.monto}", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }
                Surface(
                    color = if (solicitud.estado == "en_camino") Color(0xFFDBEAFE) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        solicitud.estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (solicitud.estado == "en_camino") Color(0xFF1E40AF) else Color(0xFF166534)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onVerMapa,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Text("Ver mapa", fontSize = 12.sp)
                }
                
                if (solicitud.estado == "aceptado") {
                    Button(
                        onClick = onEnCamino,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("En camino", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onFinalizar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Finalizar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
