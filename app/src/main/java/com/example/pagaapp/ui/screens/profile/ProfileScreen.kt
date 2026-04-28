package com.example.pagaapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.screens.login.AuthViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val profile = uiState.profile ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        item {
            ProfileHeader(profile)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            ProfileStatsSection(profile)
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            ProfileSettingsSection(
                profile = profile,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
