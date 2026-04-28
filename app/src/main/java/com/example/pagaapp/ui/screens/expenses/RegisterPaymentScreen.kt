package com.example.pagaapp.ui.screens.expenses

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
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
import coil.compose.AsyncImage
import com.example.pagaapp.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    navController: NavController,
    debtId: String?,
    expensesViewModel: ExpensesViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by expensesViewModel.uiState.collectAsState()
    val debt = uiState.youOweList.find { it.id == debtId } ?: uiState.owedToYouList.find { it.id == debtId }

    var amount by remember { mutableStateOf(debt?.amount?.toString() ?: "") }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var expanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempPhotoUri
        }
    }

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PAYMENT_${timeStamp}_", ".jpg", storageDir)
    }

    val methods = listOf("Bank Transfer", "Cash", "Credit Card", "Digital Wallet")

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
        containerColor = AppBackground,
        bottomBar = {
            Button(
                onClick = {
                    if (debtId != null && amount.isNotEmpty()) {
                        expensesViewModel.registerPayment(
                            context = context,
                            debtId = debtId,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            method = selectedMethod,
                            imageUri = selectedImageUri?.toString()
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Confirm Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Debt Details Card
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

            // Payment Amount
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment Amount", fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = PrimaryGreen,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text("0,00") }
                )
            }

            // Payment Method
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = PrimaryGreen,
                            unfocusedIndicatorColor = Color.Transparent
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

            // Upload Payment Proof
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Upload Payment Proof", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
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
                                "Tap to select source",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Payment Proof",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Select Image Source") },
                text = { Text("Choose how you want to upload your payment proof.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            val photoFile = createImageFile(context)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                    }
                }
            )
        }
    }
}
