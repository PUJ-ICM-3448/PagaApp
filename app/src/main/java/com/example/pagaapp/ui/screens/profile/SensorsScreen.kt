package com.example.pagaapp.ui.screens.profile

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.theme.AppBackground
import com.example.pagaapp.ui.theme.CardBackground
import com.example.pagaapp.ui.theme.PrimaryGreen
import com.example.pagaapp.ui.theme.TextPrimary
import com.example.pagaapp.ui.theme.TextSecondary
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(navController: NavController) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val accelSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var lightLevel by remember { mutableStateOf(0f) }
    var accelMagnitude by remember { mutableStateOf(0f) }
    
    var lastMovementAlertTime by remember { mutableLongStateOf(0L) }
    var showMovementDialog by remember { mutableStateOf(false) }
    var isShaking by remember { mutableStateOf(false) }
    
    // Umbral de agitación: 1g es ~9.8. 14f detecta una agitación clara.
    val shakeThreshold = 14f 
    val cooldownMillis = 7000L 

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_LIGHT -> {
                        lightLevel = event.values[0]
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        // Magnitud total incluyendo gravedad
                        accelMagnitude = sqrt(x * x + y * y + z * z)
                        
                        if (accelMagnitude > shakeThreshold) {
                            isShaking = true
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastMovementAlertTime > cooldownMillis) {
                                showMovementDialog = true
                                lastMovementAlertTime = currentTime
                            }
                        } else if (accelMagnitude < 11.5f) {
                             isShaking = false
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        lightSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        accelSensor?.let {
            // SENSOR_DELAY_GAME para mayor respuesta al agitar
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Dialogo de propuesta de valor
    if (showMovementDialog) {
        AlertDialog(
            onDismissRequest = { showMovementDialog = false },
            icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = PrimaryGreen) },
            title = { Text(text = "¡Movimiento detectado!", fontWeight = FontWeight.Bold) },
            text = { Text("¿Necesitas efectivo ahora? Aprovecha nuestro servicio de Cash Delivery mientras estás en camino.") },
            confirmButton = {
                Button(
                    onClick = {
                        showMovementDialog = false
                        navController.navigate(Routes.RequestCash.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Pedir Efectivo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMovementDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hardware & Sensores", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Integración con Dispositivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Info GPS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Localización GPS: Activa para rutas de repartidores y puntos de retiro.",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }
            
            // Sensor de Luz
            if (lightSensor != null) {
                val isLowLight = lightLevel < 10f
                SensorCard(
                    title = "Sensor de Luz",
                    value = "${String.format("%.1f", lightLevel)} lux",
                    icon = Icons.Default.LightMode,
                    description = if (isLowLight) "Baja luz: Se sugiere modo noche." else "Iluminación óptima.",
                    isAlert = isLowLight
                )
            } else {
                SensorCard(
                    title = "Sensor de Luz",
                    value = "No disponible",
                    icon = Icons.Default.LightMode,
                    description = "Hardware no detectado.",
                    isError = true
                )
            }

            // Acelerómetro con visualización de magnitud
            if (accelSensor != null) {
                SensorCard(
                    title = "Acelerómetro",
                    value = "${String.format("%.2f", accelMagnitude)} m/s²",
                    icon = Icons.Default.Speed,
                    description = if (isShaking) "¡Agitación detectada! Sugerencia activa." else "Acelerómetro activo. Agita el celular para probar.",
                    isAlert = isShaking
                )
            } else {
                SensorCard(
                    title = "Acelerómetro",
                    value = "No disponible",
                    icon = Icons.Default.Speed,
                    description = "Hardware no detectado.",
                    isError = true
                )
            }

            // Banner instructivo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prueba de Hardware", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Agita el teléfono con firmeza para disparar el acceso directo a Cash Delivery basado en movimiento brusco.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SensorCard(
    title: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    description: String,
    isAlert: Boolean = false,
    isError: Boolean = false
) {
    val statusColor = when {
        isError -> Color.Red
        isAlert -> Color(0xFFF57C00)
        else -> PrimaryGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(value, fontWeight = FontWeight.ExtraBold, color = statusColor, fontSize = 18.sp)
                Text(
                    description, 
                    color = if (isAlert || isError) statusColor else TextSecondary, 
                    fontSize = 12.sp, 
                    lineHeight = 15.sp
                )
            }
        }
    }
}
