package com.example.pagaapp.ui.screens.expenses

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pagaapp.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    navController: NavController,
    debtId: String?,
    expensesViewModel: ExpensesViewModel = viewModel()
) {
    val uiState by expensesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val debt = uiState.youOweList.find { it.id == debtId } ?: uiState.owedToYouList.find { it.id == debtId }

    var amount by remember { mutableStateOf(debt?.amount?.toString() ?: "") }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Cámara y Galería
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            expensesViewModel.setEvidenceUri(tempUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { expensesViewModel.setEvidenceUri(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = createImageFile(context)
            tempUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraLauncher.launch(tempUri!!)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val methods = listOf("Bank Transfer", "Cash", "Credit Card", "Digital Wallet")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar Evidencia") },
            text = { Text("¿Deseas tomar una foto o elegir una de la galería?") },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                    showDialog = false
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) {
                    Text("Galería")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register Payment", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (debt != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Debt Details",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = debt.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = debt.description, color = TextSecondary, fontSize = 14.sp)
                            }
                            Text(
                                text = "$${String.format("%.2f", debt.amount)}",
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment Amount", fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryGreen
                    ),
                    placeholder = { Text("0,00") }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment Method", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, "contentDescription")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethod = method
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Upload Payment Proof", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.evidenceUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(uiState.evidenceUri),
                                contentDescription = "Evidence",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { expensesViewModel.setEvidenceUri(null) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.Red)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Upload screenshot or receipt",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                "Camera or Gallery",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = { 
                    // Lógica para registrar el pago
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("REGISTER PAYMENT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
