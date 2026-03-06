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
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    state: UiState<List<Investment>>,
    actionState: UiState<String>,
    onRefresh: () -> Unit,
    onCreate: (CreateInvestmentRequest) -> Unit,
    onUpdate: (Int, UpdateInvestmentRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingInv by remember { mutableStateOf<Investment?>(null) }
    var deletingInv by remember { mutableStateOf<Investment?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> { snackbarHostState.showSnackbar(actionState.data); onClearAction() }
            is UiState.Error -> { snackbarHostState.showSnackbar(actionState.message); onClearAction() }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Investments") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PrimaryGreen) { Icon(Icons.Default.Add, contentDescription = "Add") } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = ExpenseRed); Spacer(Modifier.height(16.dp)); Button(onClick = onRefresh) { Text("Retry") } }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(Modifier.height(16.dp)); Text("No investments", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        }
                    }
                } else {
                    val totalValue = state.data.sumOf { it.currentValue }
                    val totalCost = state.data.sumOf { it.costBasis }
                    val totalGain = totalValue - totalCost
                    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        item {
                            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Portfolio Value", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                        Text(formatCurrency(totalValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Gain/Loss", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                        Text(formatCurrency(totalGain), style = MaterialTheme.typography.titleLarge, color = if (totalGain >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        items(state.data) { inv ->
                            val gain = inv.currentValue - inv.costBasis
                            Card(onClick = { editingInv = inv }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(inv.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                            if (inv.ticker != null) { Spacer(Modifier.width(8.dp)); Text(inv.ticker, style = MaterialTheme.typography.bodySmall, color = InfoBlue) }
                                        }
                                        Text(inv.type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                        if (inv.shares != null) { Text("${inv.shares} shares", style = MaterialTheme.typography.bodySmall, color = TextTertiary) }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatCurrency(inv.currentValue), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${if (gain >= 0) "+" else ""}${formatCurrency(gain)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (gain >= 0) IncomeGreen else ExpenseRed
                                        )
                                    }
                                    IconButton(onClick = { deletingInv = inv }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary) }
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
        InvestmentDialog(title = "Add Investment", onDismiss = { showAddDialog = false }) { name, type, value, cost, shares, ticker ->
            onCreate(CreateInvestmentRequest(name, type, value, cost, shares, ticker)); showAddDialog = false
        }
    }
    if (editingInv != null) {
        InvestmentDialog(title = "Edit Investment", initial = editingInv, onDismiss = { editingInv = null }) { name, type, value, cost, shares, ticker ->
            onUpdate(editingInv!!.id, UpdateInvestmentRequest(name, type, value, cost, shares, ticker)); editingInv = null
        }
    }
    if (deletingInv != null) {
        AlertDialog(onDismissRequest = { deletingInv = null }, title = { Text("Delete Investment") }, text = { Text("Delete \"${deletingInv!!.name}\"?") },
            confirmButton = { TextButton(onClick = { onDelete(deletingInv!!.id); deletingInv = null }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { deletingInv = null }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentDialog(title: String, initial: Investment? = null, onDismiss: () -> Unit, onConfirm: (String, String, Double, Double, Double?, String?) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "stock") }
    var currentValue by remember { mutableStateOf(initial?.currentValue?.toString() ?: "") }
    var costBasis by remember { mutableStateOf(initial?.costBasis?.toString() ?: "0") }
    var shares by remember { mutableStateOf(initial?.shares?.toString() ?: "") }
    var ticker by remember { mutableStateOf(initial?.ticker ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }
    val types = listOf("stock", "etf", "mutual_fund", "bond", "crypto", "real_estate", "other")

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(value = type.replaceFirstChar { it.uppercase() }.replace("_", " "), onValueChange = {}, readOnly = true, label = { Text("Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }.replace("_", " ")) }, onClick = { type = t; typeExpanded = false }) }
                    }
                }
                OutlinedTextField(value = currentValue, onValueChange = { currentValue = it }, label = { Text("Current Value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = costBasis, onValueChange = { costBasis = it }, label = { Text("Cost Basis") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = shares, onValueChange = { shares = it }, label = { Text("Shares (optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, type, currentValue.toDoubleOrNull() ?: 0.0, costBasis.toDoubleOrNull() ?: 0.0, shares.toDoubleOrNull(), ticker.ifBlank { null }) }, enabled = name.isNotBlank() && currentValue.isNotBlank()) { Text("Save", color = PrimaryGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
