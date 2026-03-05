package com.oasis.budgeting.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.budgeting.data.model.BudgetCategory
import com.oasis.budgeting.data.model.BudgetGroup
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.BudgetData
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    state: UiState<BudgetData>,
    actionState: UiState<String>,
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAssignBudget: (Int, Double) -> Unit,
    onRefresh: () -> Unit,
    onClearAction: () -> Unit
) {
    var editingCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(actionState.data)
                onClearAction()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(actionState.message)
                onClearAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
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

                    // Summary Bar
                    if (data.summary != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                BudgetSummaryItem("Budgeted", data.summary.totalBudgeted)
                                BudgetSummaryItem("Activity", data.summary.totalActivity)
                                BudgetSummaryItem(
                                    "To Budget",
                                    data.summary.toBeBudgeted,
                                    highlight = true
                                )
                            }
                        }
                    }

                    // Budget Groups
                    if (data.budget != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            // Header row
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Category",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                    Text(
                                        "Assigned",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        "Activity",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        "Available",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            data.budget.groups.forEach { group ->
                                item {
                                    BudgetGroupHeader(group)
                                }
                                items(group.categories) { category ->
                                    BudgetCategoryRow(
                                        category = category,
                                        onEditAssigned = { editingCategory = category }
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (editingCategory != null) {
        AssignBudgetDialog(
            category = editingCategory!!,
            onDismiss = { editingCategory = null },
            onConfirm = { amount ->
                onAssignBudget(editingCategory!!.id, amount)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun BudgetSummaryItem(label: String, amount: Double, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            color = when {
                highlight && amount > 0 -> IncomeGreen
                highlight && amount < 0 -> ExpenseRed
                amount < 0 -> ExpenseRed
                else -> TextPrimary
            },
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun BudgetGroupHeader(group: BudgetGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                group.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.4f)
            )
            Text(
                formatCurrency(group.assigned),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                formatCurrency(group.activity),
                style = MaterialTheme.typography.bodySmall,
                color = if (group.activity < 0) ExpenseRed else TextSecondary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                formatCurrency(group.available),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    group.available < 0 -> ExpenseRed
                    group.available > 0 -> IncomeGreen
                    else -> TextSecondary
                },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun BudgetCategoryRow(category: BudgetCategory, onEditAssigned: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.4f)
        )
        Text(
            formatCurrency(category.assigned),
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryGreen,
            modifier = Modifier
                .weight(1f)
                .clickable { onEditAssigned() },
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium
        )
        Text(
            formatCurrency(category.activity),
            style = MaterialTheme.typography.bodyMedium,
            color = if (category.activity < 0) ExpenseRed else TextSecondary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            formatCurrency(category.available),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                category.available < 0 -> ExpenseRed
                category.available > 0 -> IncomeGreen
                else -> TextSecondary
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AssignBudgetDialog(
    category: BudgetCategory,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(category.assigned.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Budget") },
        text = {
            Column {
                Text(
                    "Category: ${category.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Assigned Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) }
            ) { Text("Assign", color = PrimaryGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
