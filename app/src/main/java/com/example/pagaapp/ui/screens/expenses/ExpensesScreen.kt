package com.example.pagaapp.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun ExpensesScreen(
    navController: NavController,
    viewModel: ExpensesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PrimaryGreen,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Debt Management",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track what you owe and what's owed to you",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // You Owe Section
            item {
                SectionHeader(
                    title = "You Owe (${uiState.youOweList.size})",
                    amount = uiState.totalYouOwe,
                    isExpense = true
                )
            }

            items(uiState.youOweList) { debt ->
                DebtCard(
                    debt = debt, 
                    showAction = true,
                    onActionClick = {
                        navController.navigate(Routes.RegisterPayment.createRoute(debt.id))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Owed To You Section
            item {
                SectionHeader(
                    title = "Owed to You (${uiState.owedToYouList.size})",
                    amount = uiState.totalOwedToYou,
                    isExpense = false
                )
            }

            items(uiState.owedToYouList) { debt ->
                DebtCard(debt = debt, showAction = false)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, amount: Double, isExpense: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = "${if (isExpense) "-" else "+"}$${String.format("%.2f", amount)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) ExpenseRed else IncomeGreen
        )
    }
}

@Composable
fun DebtCard(debt: DebtItem, showAction: Boolean = false, onActionClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = debt.avatarColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = debt.initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = debt.description,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = debt.date,
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }

                // Amount and Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", debt.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (debt.avatarColor == IncomeGreen) IncomeGreen else ExpenseRed
                    )
                    
                    val statusColor = if (debt.status == DebtStatus.PAID) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                    val statusTextColor = if (debt.status == DebtStatus.PAID) Color(0xFF059669) else Color(0xFFD97706)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = debt.status.name.lowercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusTextColor
                        )
                    }
                }
            }

            if (showAction) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Payment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
