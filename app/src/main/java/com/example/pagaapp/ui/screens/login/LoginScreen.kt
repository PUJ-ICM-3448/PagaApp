package com.example.pagaapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.theme.PrimaryGreen

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF16A34A),
                        Color(0xFF15803D)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PAGAPP",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Manage shared expenses with friends",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { 
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Login",
                    color = PrimaryGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Don't have an account? Register",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.5f))
                Text(
                    text = " Or continue with ",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SocialButton(text = "G")
                SocialButton(text = "f")
                SocialButton(text = "")
            }
        }
    }
}

@Composable
fun SocialButton(text: String) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
