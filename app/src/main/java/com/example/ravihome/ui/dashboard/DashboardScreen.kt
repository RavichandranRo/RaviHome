package com.example.ravihome.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    onEbClick: () -> Unit,
    onTravelClick: () -> Unit,
    onPaymentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Planned: ${summary.planned}")
            Text(text = "Completed: ${summary.completed}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onEbClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "EB calculator")
        }

        Button(onClick = onTravelClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Travel status")
        }

        Button(onClick = onPaymentsClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Payments")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    DashboardScreen(
        summary = DashboardSummary(planned = 4, completed = 7),
        onEbClick = {},
        onTravelClick = {},
        onPaymentsClick = {}
    )
}