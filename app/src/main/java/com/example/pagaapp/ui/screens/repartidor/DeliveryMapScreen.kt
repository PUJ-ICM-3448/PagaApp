package com.example.pagaapp.ui.screens.repartidor

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pagaapp.ui.theme.PrimaryGreen
import com.example.pagaapp.ui.theme.AppBackground
import com.example.pagaapp.ui.theme.CardBackground
import com.example.pagaapp.ui.theme.TextPrimary
import com.example.pagaapp.ui.theme.TextSecondary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapScreen(
    solicitudId: String,
    navController: NavController,
    viewModel: DeliveryMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Sensores: Sensor de Luz para modo nocturno automático o alerta
    var lightLevel by remember { mutableFloatStateOf(0f) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    lightLevel = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Cámara y Galería
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.setEvidenceUri(tempUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.setEvidenceUri(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = createImageFile(context)
            tempUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraLauncher.launch(tempUri!!)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setLocationClient(LocationServices.getFusedLocationProviderClient(context))
        viewModel.startTracking(solicitudId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Entrega en curso", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (lightLevel < 10f) {
                            Text("Entorno oscuro - Conduce con cuidado", color = Color.Yellow, fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (lightLevel < 10f) Color(0xFF1A1A1A) else PrimaryGreen)
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

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    properties = MapProperties(
                        mapStyleOptions = if (lightLevel < 10f) MapStyleOptions(DARK_MAP_STYLE) else null
                    )
                ) {
                    Marker(
                        state = rememberMarkerState(position = clienteLocation),
                        title = "Cliente: ${uiState.clienteNombre}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    Marker(
                        state = rememberMarkerState(position = repartidorLocation),
                        title = "Tu ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Panel inferior con detalles y controles de evidencia
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
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
                            }
                            // Badge de estado
                            Surface(
                                color = when(uiState.estado) {
                                    "aceptado" -> Color.Gray
                                    "en_camino" -> Color(0xFF3B82F6)
                                    "entregado" -> Color(0xFF10B981)
                                    else -> Color.LightGray
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = uiState.estado.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.estado == "en_camino") {
                            Text("Evidencia de entrega:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.evidenceUri != null) {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.Black, RoundedCornerShape(12.dp))) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uiState.evidenceUri),
                                        contentDescription = "Evidencia",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.setEvidenceUri(null) },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Quitar", tint = Color.White)
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cámara")
                                    }
                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Galería")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (uiState.estado == "aceptado") {
                            Button(
                                onClick = { viewModel.actualizarEstado("en_camino") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("MARCAR COMO EN CAMINO", fontWeight = FontWeight.Bold)
                            }
                        } else if (uiState.estado == "en_camino") {
                            Button(
                                onClick = {
                                    viewModel.finalizarPedidoConEvidencia {
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.evidenceUri != null && !uiState.isUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                if (uiState.isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("FINALIZAR ENTREGA", fontWeight = FontWeight.Bold)
                                }
                            }
                            if (uiState.error != null) {
                                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
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

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

// Estilo de mapa oscuro (JSON simplificado para ejemplo)
private const val DARK_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [{ "color": "#242f3e" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#746855" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#242f3e" }] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#d59563" }] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [{ "color": "#d59563" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#38414e" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#212a37" }] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#9ca5b3" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#17263c" }] }
]
"""
