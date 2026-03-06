package com.oasis.budgeting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oasis.budgeting.data.model.SettingsUpdateRequest
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.AppSettings
import com.oasis.budgeting.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UiState<AppSettings>,
    actionState: UiState<String>,
    onUpdateSettings: (SettingsUpdateRequest) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onClearAction: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> { snackbarHostState.showSnackbar(actionState.data); onClearAction() }
            is UiState.Error -> { snackbarHostState.showSnackbar(actionState.message); onClearAction() }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = ExpenseRed); Spacer(Modifier.height(16.dp)); Button(onClick = onRefresh) { Text("Retry") } }
            }
            is UiState.Success -> {
                val settings = state.data
                var serverUrl by remember(settings) { mutableStateOf(settings.serverUrl) }
                var currency by remember(settings) { mutableStateOf(settings.settings.currency) }
                var locale by remember(settings) { mutableStateOf(settings.settings.locale) }
                var theme by remember(settings) { mutableStateOf(settings.settings.theme) }
                var showLogoutConfirm by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Server Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = serverUrl,
                                onValueChange = { serverUrl = it },
                                label = { Text("Server URL") },
                                leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { onUpdateServerUrl(serverUrl) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) { Text("Update Server URL") }
                        }
                    }

                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("App Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Currency") },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = locale,
                                onValueChange = { locale = it },
                                label = { Text("Locale") },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = theme,
                                onValueChange = { theme = it },
                                label = { Text("Theme") },
                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    onUpdateSettings(SettingsUpdateRequest(
                                        currency = currency,
                                        locale = locale,
                                        theme = theme
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) { Text("Save Settings") }
                        }
                    }

                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(Modifier.padding(16.dp)) {
                            Button(
                                onClick = { showLogoutConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Logout")
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }

                if (showLogoutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        title = { Text("Logout") },
                        text = { Text("Are you sure you want to logout?") },
                        confirmButton = { TextButton(onClick = { showLogoutConfirm = false; onLogout() }) { Text("Logout", color = ExpenseRed) } },
                        dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
                    )
                }
            }
            else -> {}
        }
    }
}
