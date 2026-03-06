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
fun TransactionsScreen(
    state: UiState<TransactionsResponse>,
    actionState: UiState<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCreate: (CreateTransactionRequest) -> Unit,
    onUpdate: (Int, UpdateTransactionRequest) -> Unit,
    onDelete: (Int) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onClearAction: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deletingTransaction by remember { mutableStateOf<Transaction?>(null) }
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
                title = { Text("Transactions") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
                    if (data.transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No transactions found", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(data.transactions) { transaction ->
                                TransactionCard(
                                    transaction = transaction,
                                    onEdit = { editingTransaction = transaction },
                                    onDelete = { deletingTransaction = transaction }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }

                        // Pagination
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onPreviousPage,
                                enabled = data.pagination.page > 1
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                                Text("Previous")
                            }
                            Text(
                                "Page ${data.pagination.page} of ${data.pagination.totalPages}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                            TextButton(
                                onClick = onNextPage,
                                enabled = data.pagination.page < data.pagination.totalPages
                            ) {
                                Text("Next")
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onCreate(it)
                showAddDialog = false
            }
        )
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            onDismiss = { editingTransaction = null },
            onConfirm = { request ->
                onUpdate(editingTransaction!!.id, request)
                editingTransaction = null
            }
        )
    }

    if (deletingTransaction != null) {
        AlertDialog(
            onDismissRequest = { deletingTransaction = null },
            title = { Text("Delete Transaction") },
            text = { Text("Delete transaction \"${deletingTransaction!!.payee}\" for ${formatCurrency(deletingTransaction!!.amount)}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(deletingTransaction!!.id)
                    deletingTransaction = null
                }) { Text("Delete", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { deletingTransaction = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        transaction.payee,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (transaction.cleared) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Cleared",
                            modifier = Modifier.size(14.dp),
                            tint = IncomeGreen
                        )
                    }
                }
                Row {
                    Text(
                        transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    if (transaction.categoryName != null) {
                        Text(" • ", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        Text(
                            transaction.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                if (!transaction.memo.isNullOrBlank()) {
                    Text(
                        transaction.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            Text(
                formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.amount >= 0) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreateTransactionRequest) -> Unit
) {
    var payee by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf("1") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var cleared by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (negative for expense)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Account ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cleared, onCheckedChange = { cleared = it })
                    Text("Cleared")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        CreateTransactionRequest(
                            accountId = accountId.toIntOrNull() ?: 1,
                            date = date,
                            payee = payee,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            memo = memo.ifBlank { null },
                            cleared = cleared
                        )
                    )
                },
                enabled = payee.isNotBlank() && amount.isNotBlank()
            ) { Text("Add", color = PrimaryGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (UpdateTransactionRequest) -> Unit
) {
    var payee by remember { mutableStateOf(transaction.payee) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var memo by remember { mutableStateOf(transaction.memo ?: "") }
    var date by remember { mutableStateOf(transaction.date) }
    var cleared by remember { mutableStateOf(transaction.cleared) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cleared, onCheckedChange = { cleared = it })
                    Text("Cleared")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        UpdateTransactionRequest(
                            payee = payee,
                            amount = amount.toDoubleOrNull(),
                            date = date,
                            memo = memo.ifBlank { null },
                            cleared = cleared
                        )
                    )
                },
                enabled = payee.isNotBlank()
            ) { Text("Save", color = PrimaryGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
