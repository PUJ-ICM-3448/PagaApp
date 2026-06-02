package com.example.pagaapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pagaapp.navigation.AppNavigation
import com.example.pagaapp.ui.theme.PagaAppTheme
import com.example.pagaapp.util.DemoDataSeeder
import com.example.pagaapp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Seed demo data (locations, etc) if needed
        DemoDataSeeder.seedDemoData()

        // Initialize Notification Channel
        NotificationHelper(this)

        // Request notification permission for Android 13+
        askNotificationPermission()

        setContent {
            PagaAppTheme {
                AppNavigation()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
                    // Permission result handled if needed
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
