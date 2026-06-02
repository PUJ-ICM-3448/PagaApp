package com.example.pagaapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.pagaapp.utils.NotificationHelper
import com.example.pagaapp.utils.AppNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val notifications by NotificationHelper.notifications.collectAsState()
    var showNotificationSheet by remember { mutableStateOf(false) }

    val debts = listOf(
        DebtItemData("MG", "Maria Garcia", "You owe", "-$45.50", false),
        DebtItemData("JL", "Juan Lopez", "Owes you", "+$28.00", true),
        DebtItemData("SM", "Sofia Martinez", "You owe", "-$15.75", false)
    )

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            HomeHeader(
                unreadCount = notifications.count { !it.isRead },
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
            items(debts) { debt ->
                DebtCard(debt)
            }
        }

        if (showNotificationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationSheet = false },
                containerColor = Color.White
            ) {
                NotificationSheetContent(notifications)
            }
        }
    }
}

@Composable
fun NotificationSheetContent(notifications: List<AppNotification>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            "Notificaciones",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No tienes notificaciones", color = Color.Gray)
                }
            }
        } else {
            LazyColumn {
                items(notifications) { notification ->
                    ListItem(
                        headlineContent = { Text(notification.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(notification.message) },
                        trailingContent = { Text(notification.timestamp, fontSize = 12.sp, color = Color.Gray) },
                        leadingContent = {
                            Surface(shape = CircleShape, color = PrimaryGreen.copy(0.1f)) {
                                Icon(Icons.Default.Notifications, null, modifier = Modifier.padding(8.dp), tint = PrimaryGreen)
                            }
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(0.3f))
                }
            }
        }
    }
}

@Composable
fun HomeHeader(unreadCount: Int, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = PrimaryGreen)) {
                Box(Modifier.padding(16.dp)) { Text("CR", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("Carlos Rodriguez", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        BadgedBox(
            badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }
        ) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.background(CardBackground, CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.Notifications, "Notifications", tint = PrimaryGreen)
            }
        }
    }
}

@Composable
fun BalanceCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Your Balance", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("$152.75", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("You owe", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("$89.25", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Owed to you", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("$242.00", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = PrimaryGreen)) {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text("+  Add Expense", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text("✈  Pay Debt", color = PrimaryGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun QuickAccessCards(navController: NavController) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickCard("Cash Points", "Find nearby", CashPointsGreen, Modifier.weight(1f)) {
            navController.navigate(Routes.Location.route)
        }
        QuickCard("Cash Delivery", "Request now", CashDeliveryBlue, Modifier.weight(1f)) {
            navController.navigate(Routes.RequestCash.route)
        }
    }
}

@Composable
fun QuickCard(title: String, subtitle: String, backgroundColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = PrimaryGreen)) {
                Box(Modifier.padding(12.dp)) { Text("●", color = Color.White) }
            }
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class DebtItemData(val initials: String, val name: String, val subtitle: String, val amount: String, val isPositive: Boolean)

@Composable
fun DebtCard(debt: DebtItemData) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = PrimaryGreen)) {
                    Box(Modifier.padding(14.dp)) { Text(debt.initials, color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(debt.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(debt.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Text(debt.amount, color = if (debt.isPositive) IncomeGreen else ExpenseRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}
