@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.pagaapp.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// --- MODELOS ---
data class PaymentCard(val id: String, val lastFour: String, val type: String)
data class ChatMessage(val text: String, val isFromUser: Boolean)
data class FAQItem(val question: String, val answer: String)

@Composable
fun SettingsDetailScreen(title: String, navController: NavHostController, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6366F1))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

// --- PANTALLA: MÉTODOS DE PAGO ---
@Composable
fun PaymentMethodsScreen(navController: NavHostController) {
    var cards by remember { mutableStateOf(listOf(PaymentCard("1", "4242", "Visa"))) }
    var showDialog by remember { mutableStateOf(false) }
    var newCardNumber by remember { mutableStateOf("") }

    SettingsDetailScreen("Métodos de Pago", navController) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cards) { card ->
                SettingRow("${card.type} **** ${card.lastFour}", "Activa", Icons.Default.CreditCard)
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(" Agregar Tarjeta")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nueva Tarjeta") },
                text = {
                    TextField(
                        value = newCardNumber,
                        onValueChange = { if (it.length <= 16) newCardNumber = it },
                        label = { Text("Número de Tarjeta") },
                        placeholder = { Text("1234123412341234") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newCardNumber.length >= 4) {
                            cards = cards + PaymentCard(
                                id = (cards.size + 1).toString(),
                                lastFour = newCardNumber.takeLast(4),
                                type = "Visa"
                            )
                            newCardNumber = ""
                            showDialog = false
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

// --- PANTALLA: AYUDA Y SOPORTE ---
@Composable
fun HelpSupportScreen(navController: NavHostController) {
    var currentView by remember { mutableStateOf("main") } // main, chat, faq, report

    when (currentView) {
        "chat" -> ChatInterface(onBack = { currentView = "main" })
        "faq" -> FAQInterface(onBack = { currentView = "main" })
        "report" -> ReportInterface(onBack = { currentView = "main" })
        else -> {
            SettingsDetailScreen("Ayuda y Soporte", navController) {
                SettingRow("Centro de Ayuda", "Preguntas frecuentes", Icons.Default.Info, onClick = { currentView = "faq" })
                SettingRow("Chat en Vivo", "Habla con nosotros ahora", Icons.AutoMirrored.Filled.Chat, onClick = { currentView = "chat" })
                SettingRow("Reportar Problema", "Envíanos un mensaje", Icons.Default.Report, onClick = { currentView = "report" })
            }
        }
    }
}

@Composable
fun FAQInterface(onBack: () -> Unit) {
    val faqs = listOf(
        FAQItem("¿Cómo agrego saldo?", "Puedes agregar saldo vinculando tu cuenta bancaria en la sección de Métodos de Pago."),
        FAQItem("¿Es segura la aplicación?", "Sí, utilizamos cifrado de grado bancario para proteger todos tus datos y transacciones."),
        FAQItem("¿Cómo cancelo un pago?", "Los pagos realizados son instantáneos, pero puedes contactar al soporte para disputas."),
        FAQItem("¿Tienen costos ocultos?", "No, PagaApp muestra todas las comisiones antes de confirmar cualquier operación.")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Preguntas Frecuentes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(faqs) { faq ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { expanded = !expanded },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(faq.question, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                        AnimatedVisibility(visible = expanded) {
                            Text(faq.answer, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportInterface(onBack: () -> Unit) {
    var reportText by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportar Problema") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            if (isSent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(64.dp))
                    Text("¡Reporte Enviado!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Text("Revisaremos tu caso en las próximas 24 horas.", modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) { Text("Volver") }
                }
            } else {
                Text("Describe el problema detalladamente:", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = reportText,
                    onValueChange = { reportText = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 12.dp),
                    placeholder = { Text("Ej: No puedo vincular mi tarjeta Visa...") }
                )
                Button(
                    onClick = { if (reportText.isNotBlank()) isSent = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Enviar Reporte") }
            }
        }
    }
}

@Composable
fun ChatInterface(onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(ChatMessage("¡Hola! ¿En qué podemos ayudarte hoy?", false))) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soporte en Vivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                items(messages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .background(
                                    if (msg.isFromUser) Color(0xFF6366F1) else Color(0xFFE5E7EB),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            color = if (msg.isFromUser) Color.White else Color.Black
                        )
                    }
                }
            }
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") }
                )
                IconButton(onClick = {
                    if (messageText.isNotBlank()) {
                        messages = messages + ChatMessage(messageText, true)
                        messageText = ""
                        messages = messages + ChatMessage("Recibido. Un agente se conectará pronto.", false)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color(0xFF6366F1))
                }
            }
        }
    }
}

// --- PANTALLA: AJUSTES DE APP ---
@Composable
fun AppSettingsScreen(navController: NavHostController) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf("Español (Latinoamérica)") }

    SettingsDetailScreen("Ajustes de la App", navController) {
        var notificationsEnabled by remember { mutableStateOf(true) }
        
        Text("Preferencias Globales", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = Color(0xFF6366F1))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificaciones", fontWeight = FontWeight.Bold)
                    Text("Alertas de gastos y pagos", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
        }
        
        SettingRow("Idioma", currentLanguage, Icons.Default.Language, onClick = { showLanguageMenu = true })
        
        if (showLanguageMenu) {
            AlertDialog(
                onDismissRequest = { showLanguageMenu = false },
                title = { Text("Seleccionar Idioma") },
                text = {
                    Column {
                        listOf("Español (Latinoamérica)", "English (US)", "Português", "Français").forEach { lang ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { 
                                    currentLanguage = lang
                                    showLanguageMenu = false
                                }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = currentLanguage == lang, onClick = null)
                                Text(lang, modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showLanguageMenu = false }) { Text("Cerrar") } }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sensores (Visual)", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        SettingRow("Acelerómetro", "Agitar para refrescar", Icons.Default.ScreenRotation)
        SettingRow("Proximidad", "Ocultar saldo", Icons.Default.VisibilityOff)
    }
}

// --- PANTALLA: SEGURIDAD ---
@Composable
fun SecurityScreen(navController: NavHostController) {
    var showPasswordFlow by remember { mutableStateOf(false) }

    if (showPasswordFlow) {
        ChangePasswordInterface(onBack = { showPasswordFlow = false })
    } else {
        SettingsDetailScreen("Seguridad", navController) {
            SettingRow("Cambiar Contraseña", "Último cambio hace 3 meses", Icons.Default.Lock, onClick = { showPasswordFlow = true })
            SettingRow("Verificación en 2 pasos", "Activado", Icons.Default.Shield)
            SettingRow("Sesiones Activas", "2 dispositivos", Icons.Default.Devices)
        }
    }
}

@Composable
fun ChangePasswordInterface(onBack: () -> Unit) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cambiar Contraseña") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            if (success) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF6366F1), modifier = Modifier.size(64.dp))
                    Text("Contraseña Actualizada", fontWeight = FontWeight.Bold)
                    Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Volver") }
                }
            } else {
                OutlinedTextField(
                    value = oldPass, onValueChange = { oldPass = it },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPass, onValueChange = { newPass = it },
                    label = { Text("Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPass, onValueChange = { confirmPass = it },
                    label = { Text("Confirmar Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { if (newPass == confirmPass && newPass.isNotBlank()) success = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) { Text("Actualizar Contraseña") }
            }
        }
    }
}
