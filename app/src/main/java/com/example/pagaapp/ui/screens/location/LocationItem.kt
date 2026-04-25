package com.example.pagaapp.ui.screens.location

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LocationItem(location: LocationModel) {

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // IZQUIERDA (icono + info)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                LocationTypeIcon(type = location.type)

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0D1B3D)
                    )

                    Text(
                        text = location.typeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7C8799)
                    )

                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9AA3B2)
                    )
                }
            }

            // DERECHA (distancia + botón)
            Column(
                horizontalAlignment = Alignment.End
            ) {

                DistanceChip(distance = location.distance)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Get directions",
                    color = Color(0xFF19A94B),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        val uri = Uri.parse("geo:0,0?q=${location.address}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun LocationTypeIcon(type: LocationType) {

    val bgColor = when (type) {
        LocationType.ATM -> Color(0xFF16A34A)
        LocationType.BANK -> Color(0xFF4F8EF7)
        LocationType.STORE -> Color(0xFFF4A300)
        LocationType.PARTNER -> Color(0xFF8B5CF6)
    }

    val icon = when (type) {
        LocationType.ATM -> Icons.Outlined.Wallet
        LocationType.BANK -> Icons.Outlined.AccountBalance
        LocationType.STORE -> Icons.Outlined.Store
        LocationType.PARTNER -> Icons.Outlined.Business
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
fun DistanceChip(distance: String) {

    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFDDF5E4),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = distance,
            color = Color(0xFF19A94B),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}