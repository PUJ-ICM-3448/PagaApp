package com.example.pagaapp.ui.screens.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LocationMapSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(Color(0xFFEDEDED))
    ) {
        MapPin(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 90.dp, y = 18.dp),
            backgroundColor = Color(0xFFF4A300),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        MapPin(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 165.dp, y = 58.dp),
            backgroundColor = Color(0xFF16A34A),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Wallet,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        MapPin(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 145.dp, y = 155.dp),
            backgroundColor = Color(0xFF16A34A),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Wallet,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        MapPin(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 270.dp, y = 170.dp),
            backgroundColor = Color(0xFF4F8EF7),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        MapPin(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 320.dp, y = 238.dp),
            backgroundColor = Color(0xFF8B5CF6),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Business,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        UserLocationDot(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 232.dp, y = 240.dp)
        )
    }
}

@Composable
private fun MapPin(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    icon: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(x = 28.dp, y = 58.dp)
                .size(width = 3.dp, height = 26.dp)
                .background(Color(0xFF4B5563))
        )

        Box(
            modifier = Modifier
                .size(58.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun UserLocationDot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .background(Color(0x336FA3FF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF5B8DEF), CircleShape)
        )
    }
}