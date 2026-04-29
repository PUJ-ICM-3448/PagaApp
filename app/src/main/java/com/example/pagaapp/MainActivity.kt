package com.example.pagaapp

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.pagaapp.navigation.AppNavigation
import com.example.pagaapp.ui.theme.PagaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
            val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
            
            var luxValue by remember { mutableStateOf(100f) } // Default to light
            
            DisposableEffect(Unit) {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.let {
                            luxValue = it.values[0]
                        }
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                
                sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
                
                onDispose {
                    sensorManager.unregisterListener(listener)
                }
            }

            // Umbral de 10 lux para activar el modo oscuro
            val isDarkTheme = luxValue < 10f

            PagaAppTheme(darkTheme = isDarkTheme) {
                // Request notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        // Handle permission result if needed
                    }
                    
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                AppNavigation()
            }
        }
    }
}
