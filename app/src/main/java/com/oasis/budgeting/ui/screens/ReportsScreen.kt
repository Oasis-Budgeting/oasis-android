package com.oasis.budgeting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.ReportsData
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: UiState<ReportsData>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    val tabs = listOf("Spending", "Income/Expense", "Net Worth", "Budget vs Actual")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PrimaryGreen,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onSelectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                    when (selectedTab) {
                        0 -> SpendingByCategoryTab(data.spendingByCategory)
                        1 -> IncomeVsExpenseTab(data.incomeVsExpense)
                        2 -> NetWorthTab(data.netWorth)
                        3 -> BudgetVsActualTab(data.budgetVsActual)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun SpendingByCategoryTab(data: List<SpendingByCategory>) {
    if (data.isEmpty()) {
        EmptyReportMessage("No spending data available")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1.5f))
                Text("Amount", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("%", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
            }
        }
        items(data) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        formatCurrency(item.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "${String.format("%.1f", item.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
        item {
            val total = data.sumOf { it.amount }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(formatCurrency(total), style = MaterialTheme.typography.titleSmall, color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IncomeVsExpenseTab(data: List<IncomeVsExpense>) {
    if (data.isEmpty()) {
        EmptyReportMessage("No income/expense data available")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Month", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f))
                Text("Income", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Expenses", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Net", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
        items(data) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.month, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(formatCurrency(item.income), style = MaterialTheme.typography.bodyMedium, color = IncomeGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(formatCurrency(item.expenses), style = MaterialTheme.typography.bodyMedium, color = ExpenseRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(
                        formatCurrency(item.net),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.net >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun NetWorthTab(data: List<NetWorth>) {
    if (data.isEmpty()) {
        EmptyReportMessage("No net worth data available")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Month", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f))
                Text("Assets", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Liabilities", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Net Worth", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
        items(data) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.month, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(formatCurrency(item.assets), style = MaterialTheme.typography.bodyMedium, color = IncomeGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(formatCurrency(item.liabilities), style = MaterialTheme.typography.bodyMedium, color = ExpenseRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(
                        formatCurrency(item.netWorth),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.netWorth >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetVsActualTab(data: List<BudgetVsActual>) {
    if (data.isEmpty()) {
        EmptyReportMessage("No budget vs actual data available")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1.5f))
                Text("Budget", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Actual", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Diff", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
        items(data) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.categoryName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.5f))
                    Text(formatCurrency(item.budgeted), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(formatCurrency(item.actual), style = MaterialTheme.typography.bodyMedium, color = ExpenseRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(
                        formatCurrency(item.difference),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.difference >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReportMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = TextTertiary)
    }
}
