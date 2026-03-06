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
import com.oasis.budgeting.data.model.Account
import com.oasis.budgeting.data.model.UpdateAccountRequest
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.UiState

private fun formatCurrency(amount: Double): String {
    return String.format("$%,.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: UiState<List<Account>>,
    actionState: UiState<String>,
    onRefresh: () -> Unit,
    onCreate: (String, String, Double, Boolean) -> Unit,
    onUpdate: (Int, UpdateAccountRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var deletingAccount by remember { mutableStateOf<Account?>(null) }
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
                title = { Text("Accounts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
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
                val accounts = state.data
                if (accounts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No accounts yet", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                            Text("Tap + to add your first account", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                    }
                } else {
                    val grouped = accounts.groupBy { it.type }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        grouped.forEach { (type, accountList) ->
                            item {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() }.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextTertiary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(accountList) { account ->
                                AccountCard(
                                    account = account,
                                    onEdit = { editingAccount = account },
                                    onDelete = { deletingAccount = account }
                                )
                            }
                            item {
                                val subtotal = accountList.sumOf { it.balance }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                    Text(
                                        formatCurrency(subtotal),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (subtotal >= 0) IncomeGreen else ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
            else -> {}
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, balance, onBudget ->
                onCreate(name, type, balance, onBudget)
                showAddDialog = false
            }
        )
    }

    if (editingAccount != null) {
        EditAccountDialog(
            account = editingAccount!!,
            onDismiss = { editingAccount = null },
            onConfirm = { request ->
                onUpdate(editingAccount!!.id, request)
                editingAccount = null
            }
        )
    }

    if (deletingAccount != null) {
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete \"${deletingAccount!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(deletingAccount!!.id)
                    deletingAccount = null
                }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccount = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AccountCard(account: Account, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (account.closed) {
                    Text("Closed", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(account.balance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (account.balance >= 0) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("checking") }
    var balance by remember { mutableStateOf("0") }
    var onBudget by remember { mutableStateOf(true) }
    var typeExpanded by remember { mutableStateOf(false) }
    val types = listOf("checking", "savings", "credit_card", "cash", "investment")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type.replaceFirstChar { it.uppercase() }.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.replaceFirstChar { it.uppercase() }.replace("_", " ")) },
                                onClick = { type = t; typeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Starting Balance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = onBudget, onCheckedChange = { onBudget = it })
                    Text("On Budget")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, type, balance.toDoubleOrNull() ?: 0.0, onBudget) },
                enabled = name.isNotBlank()
            ) { Text("Add", color = PrimaryGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (UpdateAccountRequest) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var closed by remember { mutableStateOf(account.closed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = closed, onCheckedChange = { closed = it })
                    Text("Closed")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(UpdateAccountRequest(name = name, closed = closed)) },
                enabled = name.isNotBlank()
            ) { Text("Save", color = PrimaryGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
