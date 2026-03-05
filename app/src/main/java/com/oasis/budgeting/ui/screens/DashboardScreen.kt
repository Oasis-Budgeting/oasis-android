package com.oasis.budgeting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oasis.budgeting.data.model.Insight
import com.oasis.budgeting.data.model.Transaction
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.DashboardData
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: UiState<DashboardData>,
    onRefresh: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudget: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = ExpenseRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) { Text("Retry") }
                    }
                }
            }

            is UiState.Success -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Total Balance Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Total Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    formatCurrency(data.totalBalance),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = if (data.totalBalance >= 0) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${data.accounts.filter { !it.closed }.size} active accounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    // Budget Summary Card
                    if (data.budgetSummary != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                onClick = onNavigateToBudget
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Budget Summary",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "View Budget",
                                            tint = TextTertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val summary = data.budgetSummary
                                    BudgetSummaryRow("Budgeted", summary.totalBudgeted)
                                    BudgetSummaryRow("Activity", summary.totalActivity)
                                    BudgetSummaryRow("Available", summary.totalAvailable)
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = DividerColor
                                    )
                                    BudgetSummaryRow(
                                        "To Be Budgeted",
                                        summary.toBeBudgeted,
                                        highlight = true
                                    )
                                }
                            }
                        }
                    }

                    // Recent Transactions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onNavigateToTransactions) {
                                Text("View All", color = PrimaryGreen)
                            }
                        }
                    }

                    if (data.recentTransactions.isEmpty()) {
                        item {
                            Text(
                                "No recent transactions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                        }
                    } else {
                        items(data.recentTransactions) { transaction ->
                            TransactionRow(transaction)
                        }
                    }

                    // Insights
                    if (data.insights.isNotEmpty()) {
                        item {
                            Text(
                                "Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(data.insights) { insight ->
                            InsightCard(insight)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun BudgetSummaryRow(label: String, amount: Double, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) TextPrimary else TextSecondary,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                highlight && amount > 0 -> IncomeGreen
                highlight && amount < 0 -> ExpenseRed
                else -> TextPrimary
            },
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.payee,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    transaction.categoryName ?: "Uncategorized",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                Text(
                    transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            Text(
                formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.amount >= 0) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InsightCard(insight: Insight) {
    val color = when (insight.severity) {
        "warning" -> WarningOrange
        "error" -> ExpenseRed
        "success" -> IncomeGreen
        else -> InfoBlue
    }
    val icon = when (insight.severity) {
        "warning" -> Icons.Default.Warning
        "error" -> Icons.Default.Error
        "success" -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = insight.severity,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    insight.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(
                    insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
