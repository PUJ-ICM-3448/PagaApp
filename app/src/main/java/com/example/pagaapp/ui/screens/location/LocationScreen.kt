package com.example.pagaapp.ui.screens.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun LocationScreen(
    navController: NavHostController,
    viewModel: LocationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        LocationTopBar(navController)

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                LocationMapSection()
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nearby Locations",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF0D1B3D)
                    )

                    Text(
                        text = "${uiState.locations.size} found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7C8799)
                    )
                }
            }

            items(uiState.locations) { location ->
                LocationItem(location)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTopBar(navController: NavHostController) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        title = {
            Column {
                Text(
                    text = "Nearby Cash Points",
                    color = Color(0xFF0D1B3D)
                )
                Text(
                    text = "Find locations near you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7C8799)
                )
            }
        },
        navigationIcon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F3F5),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF374151)
                    )
                }
            }
        },
        actions = {
            Surface(
                shape = CircleShape,
                color = Color(0xFF16A34A),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    )
}