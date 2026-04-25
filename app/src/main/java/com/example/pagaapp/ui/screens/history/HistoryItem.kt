package com.example.pagaapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HistoryItem(item: HistoryModel) {

    val isIncome = item.type == TransactionType.INCOME

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            if (isIncome) Color(0xFFDDF5E4) else Color(0xFFFEE2E2),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome)
                            Icons.Outlined.ArrowDownward
                        else
                            Icons.Outlined.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) Color(0xFF16A34A) else Color.Red
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(item.title, fontWeight = FontWeight.SemiBold)
                    Text(item.category, color = Color.Gray)
                    Text(item.date, color = Color.LightGray)
                }
            }

            Text(
                text = (if (isIncome) "+" else "") + "$${item.amount}",
                color = if (isIncome) Color(0xFF16A34A) else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}