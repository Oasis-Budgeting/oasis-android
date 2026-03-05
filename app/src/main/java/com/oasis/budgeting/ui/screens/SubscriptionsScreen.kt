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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    state: UiState<List<Subscription>>,
    actionState: UiState<String>,
    onRefresh: () -> Unit,
    onCreate: (CreateSubscriptionRequest) -> Unit,
    onUpdate: (Int, UpdateSubscriptionRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSub by remember { mutableStateOf<Subscription?>(null) }
    var deletingSub by remember { mutableStateOf<Subscription?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> { snackbarHostState.showSnackbar(actionState.data); onClearAction() }
            is UiState.Error -> { snackbarHostState.showSnackbar(actionState.message); onClearAction() }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Subscriptions") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PrimaryGreen) { Icon(Icons.Default.Add, contentDescription = "Add") } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (state) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = ExpenseRed); Spacer(Modifier.height(16.dp)); Button(onClick = onRefresh) { Text("Retry") } }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(Modifier.height(16.dp))
                            Text("No subscriptions", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        }
                    }
                } else {
                    val totalMonthly = state.data.filter { it.active }.sumOf {
                        when (it.frequency.lowercase()) {
                            "yearly" -> it.amount / 12
                            "quarterly" -> it.amount / 3
                            "weekly" -> it.amount * 4.33
                            else -> it.amount
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Monthly Cost", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    Text(formatCurrency(totalMonthly), style = MaterialTheme.typography.headlineSmall, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        items(state.data) { sub ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground), onClick = { editingSub = sub }) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sub.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                            if (!sub.active) { Spacer(Modifier.width(8.dp)); Text("Inactive", style = MaterialTheme.typography.bodySmall, color = TextTertiary) }
                                        }
                                        Text("${sub.frequency.replaceFirstChar { it.uppercase() }} • Next: ${sub.nextDate}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    }
                                    Text(formatCurrency(sub.amount), style = MaterialTheme.typography.bodyLarge, color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = { deletingSub = sub }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary) }
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
        SubscriptionDialog(title = "Add Subscription", onDismiss = { showAddDialog = false }) { name, amount, freq, date ->
            onCreate(CreateSubscriptionRequest(name, amount, freq, date)); showAddDialog = false
        }
    }
    if (editingSub != null) {
        SubscriptionDialog(title = "Edit Subscription", initial = editingSub, onDismiss = { editingSub = null }) { name, amount, freq, date ->
            onUpdate(editingSub!!.id, UpdateSubscriptionRequest(name, amount, freq, date)); editingSub = null
        }
    }
    if (deletingSub != null) {
        AlertDialog(onDismissRequest = { deletingSub = null }, title = { Text("Delete Subscription") }, text = { Text("Delete \"${deletingSub!!.name}\"?") },
            confirmButton = { TextButton(onClick = { onDelete(deletingSub!!.id); deletingSub = null }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { deletingSub = null }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionDialog(title: String, initial: Subscription? = null, onDismiss: () -> Unit, onConfirm: (String, Double, String, String) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "monthly") }
    var nextDate by remember { mutableStateOf(initial?.nextDate ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var freqExpanded by remember { mutableStateOf(false) }
    val frequencies = listOf("weekly", "monthly", "quarterly", "yearly")

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = !freqExpanded }) {
                    OutlinedTextField(value = frequency.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Frequency") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                        frequencies.forEach { f -> DropdownMenuItem(text = { Text(f.replaceFirstChar { it.uppercase() }) }, onClick = { frequency = f; freqExpanded = false }) }
                    }
                }
                OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("Next Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, amount.toDoubleOrNull() ?: 0.0, frequency, nextDate) }, enabled = name.isNotBlank() && amount.isNotBlank()) { Text("Save", color = PrimaryGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
