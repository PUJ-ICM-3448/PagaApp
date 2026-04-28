package com.example.pagaapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.theme.*
import com.example.pagaapp.utils.AppNotification
import com.example.pagaapp.utils.NotificationHelper
import com.example.pagaapp.ui.screens.login.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val notifications by NotificationHelper.notifications.collectAsState()
    var showNotificationSheet by remember { mutableStateOf(false) }

    // Obtenemos el usuario actual del AuthViewModel
    val currentUser = AuthViewModel.currentUser

    val debts = listOf(
        DebtItemData("MG", "Maria Garcia", "You owe", "-$45.50", false),
        DebtItemData("JL", "Juan Lopez", "Owes you", "+$28.00", true),
        DebtItemData("SM", "Sofia Martinez", "You owe", "-$15.75", false)
    )

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            HomeHeader(
                notifications = notifications,
                userName = currentUser?.name ?: "Usuario",
                userInitials = currentUser?.initials ?: "U",
                onNotificationsClick = { 
                    showNotificationSheet = true 
                    NotificationHelper.markAsRead()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BalanceCard() }
            item { ActionButtonsRow() }
            item { QuickAccessCards(navController) }
            item {
                Text(
                    text = "Pending Debts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            items(debts) { debt -> DebtCard(debt) }
        }

        if (showNotificationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                NotificationSheetContent(notifications)
            }
        }
    }
}

@Composable
fun NotificationSheetContent(notifications: List<AppNotification>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = TextPrimary
        )
        
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Text("No notifications yet", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                    HorizontalDivider(color = Color(0xFFF3F4F6).copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryGreen.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = null, 
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun BalanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Your Available Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text(
                text = "$152.75",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    notifications: List<AppNotification>,
    userName: String,
    userInitials: String,
    onNotificationsClick: () -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryGreen) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(userInitials, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        
        IconButton(onClick = onNotificationsClick) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(unreadCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = "Notifications", 
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtonsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Send Payment")
        }
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen)
        ) {
            Text("Request")
        }
    }
}

@Composable
fun QuickAccessCards(navController: NavController) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.weight(1f).clickable { navController.navigate(Routes.Location.route) }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cash Points", fontWeight = FontWeight.Bold)
                Text("Find nearby", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.weight(1f).clickable { navController.navigate(Routes.Tracking.route) }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Delivery", fontWeight = FontWeight.Bold)
                Text("Track order", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

data class DebtItemData(val initials: String, val name: String, val subtitle: String, val amount: String, val isPositive: Boolean)

@Composable
fun DebtCard(debt: DebtItemData) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFFF3F4F6)) {
                Box(modifier = Modifier.padding(10.dp)) { Text(debt.initials) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(debt.name, fontWeight = FontWeight.Bold)
                Text(debt.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(debt.amount, color = if (debt.isPositive) IncomeGreen else ExpenseRed, fontWeight = FontWeight.Bold)
        }
    }
}
