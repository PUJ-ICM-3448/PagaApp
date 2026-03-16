package com.example.pagaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pagaapp.navigation.AppNavigation
import com.example.pagaapp.ui.theme.PagaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PagaAppTheme {
                AppNavigation()
            }
        }
    }
}
