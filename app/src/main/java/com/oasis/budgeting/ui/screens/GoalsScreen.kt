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
fun GoalsScreen(
    state: UiState<List<Goal>>,
    actionState: UiState<String>,
    onRefresh: () -> Unit,
    onCreate: (CreateGoalRequest) -> Unit,
    onUpdate: (Int, UpdateGoalRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onContribute: (Int, Double) -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var deletingGoal by remember { mutableStateOf<Goal?>(null) }
    var contributingGoal by remember { mutableStateOf<Goal?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> { snackbarHostState.showSnackbar(actionState.data); onClearAction() }
            is UiState.Error -> { snackbarHostState.showSnackbar(actionState.message); onClearAction() }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Goals") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
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
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(Modifier.height(16.dp)); Text("No goals yet", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(state.data) { goal ->
                            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                            Card(onClick = { editingGoal = goal }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Row {
                                            IconButton(onClick = { contributingGoal = goal }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.AddCircle, contentDescription = "Contribute", tint = PrimaryGreen) }
                                            IconButton(onClick = { deletingGoal = goal }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary) }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = PrimaryGreen,
                                        trackColor = DarkSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${formatCurrency(goal.currentAmount)} / ${formatCurrency(goal.targetAmount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                    }
                                    if (goal.targetDate != null) {
                                        Text("Target: ${goal.targetDate}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    }
                                    if (goal.category != null) {
                                        Text("Category: ${goal.category}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    }
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
        GoalDialog(title = "Add Goal", onDismiss = { showAddDialog = false }) { name, target, current, date, category ->
            onCreate(CreateGoalRequest(name, target, current, date, category)); showAddDialog = false
        }
    }
    if (editingGoal != null) {
        GoalDialog(title = "Edit Goal", initial = editingGoal, onDismiss = { editingGoal = null }) { name, target, current, date, category ->
            onUpdate(editingGoal!!.id, UpdateGoalRequest(name, target, current, date, category)); editingGoal = null
        }
    }
    if (deletingGoal != null) {
        AlertDialog(onDismissRequest = { deletingGoal = null }, title = { Text("Delete Goal") }, text = { Text("Delete \"${deletingGoal!!.name}\"?") },
            confirmButton = { TextButton(onClick = { onDelete(deletingGoal!!.id); deletingGoal = null }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { deletingGoal = null }) { Text("Cancel") } })
    }
    if (contributingGoal != null) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { contributingGoal = null }, title = { Text("Contribute to ${contributingGoal!!.name}") },
            text = {
                Column {
                    Text("Current: ${formatCurrency(contributingGoal!!.currentAmount)} / ${formatCurrency(contributingGoal!!.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = { onContribute(contributingGoal!!.id, amount.toDoubleOrNull() ?: 0.0); contributingGoal = null }, enabled = amount.isNotBlank()) { Text("Contribute", color = PrimaryGreen) } },
            dismissButton = { TextButton(onClick = { contributingGoal = null }) { Text("Cancel") } })
    }
}

@Composable
private fun GoalDialog(title: String, initial: Goal? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Double, String?, String?) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var targetAmount by remember { mutableStateOf(initial?.targetAmount?.toString() ?: "") }
    var currentAmount by remember { mutableStateOf(initial?.currentAmount?.toString() ?: "0") }
    var targetDate by remember { mutableStateOf(initial?.targetDate ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetAmount, onValueChange = { targetAmount = it }, label = { Text("Target Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = currentAmount, onValueChange = { currentAmount = it }, label = { Text("Current Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetDate, onValueChange = { targetDate = it }, label = { Text("Target Date (YYYY-MM-DD, optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, targetAmount.toDoubleOrNull() ?: 0.0, currentAmount.toDoubleOrNull() ?: 0.0, targetDate.ifBlank { null }, category.ifBlank { null }) }, enabled = name.isNotBlank() && targetAmount.isNotBlank()) { Text("Save", color = PrimaryGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
