package com.example.pagaapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HistoryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16A34A))
            .padding(16.dp)
    ) {
        Text(
            text = "Financial History",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Total Income"
            )

            SummaryCard(
                title = "Total Expense"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilterTabs()
    }
}

@Composable
fun RowScope.SummaryCard(
    title: String
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F4F6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FilterTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilterButton("All", true)
        FilterButton("Income", false)
        FilterButton("Expense", false)
    }
}

@Composable
fun FilterButton(
    text: String,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFF22C55E) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF475467)
        )
    }
}