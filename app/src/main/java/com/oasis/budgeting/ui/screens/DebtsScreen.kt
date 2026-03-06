package com.oasis.budgeting.ui.screens

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
import androidx.compose.ui.unit.dp
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.DebtsData
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    state: UiState<DebtsData>,
    actionState: UiState<String>,
    onRefresh: () -> Unit,
    onCreate: (CreateDebtRequest) -> Unit,
    onUpdate: (Int, UpdateDebtRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }
    var deletingDebt by remember { mutableStateOf<Debt?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> { snackbarHostState.showSnackbar(actionState.data); onClearAction() }
            is UiState.Error -> { snackbarHostState.showSnackbar(actionState.message); onClearAction() }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Debts") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PrimaryGreen) { Icon(Icons.Default.Add, contentDescription = "Add") } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = ExpenseRed); Spacer(Modifier.height(16.dp)); Button(onClick = onRefresh) { Text("Retry") } }
            }
            is UiState.Success -> {
                val data = state.data
                if (data.debts.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(Modifier.height(16.dp)); Text("No debts tracked", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        }
                    }
                } else {
                    val totalDebt = data.debts.sumOf { it.balance }
                    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        item {
                            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Debt", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    Text(formatCurrency(totalDebt), style = MaterialTheme.typography.headlineSmall, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (data.strategies != null) {
                            item {
                                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Payoff Strategies", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("Avalanche", style = MaterialTheme.typography.bodySmall, color = InfoBlue)
                                                Text("${data.strategies.payoffMonthsAvalanche} months", style = MaterialTheme.typography.bodyMedium)
                                                Text("Interest: ${formatCurrency(data.strategies.totalInterestAvalanche)}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Snowball", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen)
                                                Text("${data.strategies.payoffMonthsSnowball} months", style = MaterialTheme.typography.bodyMedium)
                                                Text("Interest: ${formatCurrency(data.strategies.totalInterestSnowball)}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        items(data.debts) { debt ->
                            Card(onClick = { editingDebt = debt }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(debt.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        Text("${debt.type.replaceFirstChar { it.uppercase() }} • ${debt.interestRate}% APR", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                        Text("Min payment: ${formatCurrency(debt.minimumPayment)}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    }
                                    Text(formatCurrency(debt.balance), style = MaterialTheme.typography.bodyLarge, color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = { deletingDebt = debt }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary) }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
            else -> {}
        }
    }

    if (showAddDialog) {
        DebtDialog(title = "Add Debt", onDismiss = { showAddDialog = false }) { name, balance, rate, minPay, type ->
            onCreate(CreateDebtRequest(name, balance, rate, minPay, type)); showAddDialog = false
        }
    }
    if (editingDebt != null) {
        DebtDialog(title = "Edit Debt", initial = editingDebt, onDismiss = { editingDebt = null }) { name, balance, rate, minPay, type ->
            onUpdate(editingDebt!!.id, UpdateDebtRequest(name, balance, rate, minPay, type)); editingDebt = null
        }
    }
    if (deletingDebt != null) {
        AlertDialog(onDismissRequest = { deletingDebt = null }, title = { Text("Delete Debt") }, text = { Text("Delete \"${deletingDebt!!.name}\"?") },
            confirmButton = { TextButton(onClick = { onDelete(deletingDebt!!.id); deletingDebt = null }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { deletingDebt = null }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtDialog(title: String, initial: Debt? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var balance by remember { mutableStateOf(initial?.balance?.toString() ?: "") }
    var rate by remember { mutableStateOf(initial?.interestRate?.toString() ?: "") }
    var minPay by remember { mutableStateOf(initial?.minimumPayment?.toString() ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "other") }
    var typeExpanded by remember { mutableStateOf(false) }
    val types = listOf("mortgage", "auto", "student", "credit_card", "personal", "other")

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Balance") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Interest Rate (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minPay, onValueChange = { minPay = it }, label = { Text("Minimum Payment") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(value = type.replaceFirstChar { it.uppercase() }.replace("_", " "), onValueChange = {}, readOnly = true, label = { Text("Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }.replace("_", " ")) }, onClick = { type = t; typeExpanded = false }) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, balance.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0, minPay.toDoubleOrNull() ?: 0.0, type) }, enabled = name.isNotBlank() && balance.isNotBlank()) { Text("Save", color = PrimaryGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
