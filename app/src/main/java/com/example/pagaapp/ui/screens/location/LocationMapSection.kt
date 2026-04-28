package com.example.pagaapp.ui.screens.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationMapSection() {
    val bogota = LatLng(4.6097, -74.0817)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 12f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = rememberMarkerState(position = bogota),
                title = "Bogotá",
                snippet = "Marker in Bogotá"
            )
        }
    }
}
