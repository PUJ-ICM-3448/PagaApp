package com.example.pagaapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pagaapp.navigation.Routes
import com.example.pagaapp.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val debts = listOf(
        DebtItemData("MG", "Maria Garcia", "You owe", "-$45.50", false),
        DebtItemData("JL", "Juan Lopez", "Owes you", "+$28.00", true),
        DebtItemData("SM", "Sofia Martinez", "You owe", "-$15.75", false)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HomeHeader() }
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

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryGreen) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("CR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Carlos Rodriguez", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryGreen)
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
