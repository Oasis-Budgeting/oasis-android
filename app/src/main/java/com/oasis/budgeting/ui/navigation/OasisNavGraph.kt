package com.oasis.budgeting.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oasis.budgeting.data.remote.RetrofitClient
import com.oasis.budgeting.data.remote.TokenManager
import com.oasis.budgeting.data.repository.*
import com.oasis.budgeting.ui.screens.*
import com.oasis.budgeting.ui.theme.*
import com.oasis.budgeting.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OasisNavGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    val apiService = remember(tokenManager) { RetrofitClient.getInstance(tokenManager) }

    // Create repositories
    val authRepository = remember { AuthRepository(apiService, tokenManager) }
    val accountRepository = remember { AccountRepository(apiService) }
    val transactionRepository = remember { TransactionRepository(apiService) }
    val budgetRepository = remember { BudgetRepository(apiService) }
    val categoryRepository = remember { CategoryRepository(apiService) }
    val reportRepository = remember { ReportRepository(apiService) }
    val subscriptionRepository = remember { SubscriptionRepository(apiService) }
    val goalRepository = remember { GoalRepository(apiService) }
    val debtRepository = remember { DebtRepository(apiService) }
    val investmentRepository = remember { InvestmentRepository(apiService) }
    val insightRepository = remember { InsightRepository(apiService) }
    val settingsRepository = remember { SettingsRepository(apiService, tokenManager) }

    // Create ViewModels using factory pattern
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAuth()
    }

    val startDestination = if (isLoggedIn) "dashboard" else "login"

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable("login") {
            val loginState by authViewModel.loginState.collectAsState()
            LaunchedEffect(loginState) {
                if (loginState is UiState.Success) {
                    navController.navigate("dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                loginState = loginState,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = {
                    authViewModel.clearLoginState()
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            val registerState by authViewModel.registerState.collectAsState()
            LaunchedEffect(registerState) {
                if (registerState is UiState.Success) {
                    navController.navigate("dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                registerState = registerState,
                onRegister = { name, username, email, password ->
                    authViewModel.register(name, username, email, password)
                },
                onNavigateToLogin = {
                    authViewModel.clearRegisterState()
                    navController.popBackStack()
                }
            )
        }

        // Dashboard
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return DashboardViewModel(accountRepository, budgetRepository, transactionRepository, insightRepository) as T
                    }
                }
            )
            val state by dashboardViewModel.state.collectAsState()
            LaunchedEffect(Unit) { dashboardViewModel.loadDashboard() }

            DashboardScreen(
                state = state,
                onRefresh = { dashboardViewModel.loadDashboard() },
                onNavigateToAccounts = { navController.navigate("accounts") },
                onNavigateToTransactions = { navController.navigate("transactions") },
                onNavigateToBudget = { navController.navigate("budget") }
            )
        }

        // Budget
        composable("budget") {
            val budgetViewModel: BudgetViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return BudgetViewModel(budgetRepository) as T
                    }
                }
            )
            val state by budgetViewModel.state.collectAsState()
            val actionState by budgetViewModel.actionState.collectAsState()
            val currentMonth by budgetViewModel.currentMonth.collectAsState()
            LaunchedEffect(Unit) { budgetViewModel.loadBudget() }

            BudgetScreen(
                state = state,
                actionState = actionState,
                currentMonth = currentMonth,
                onPreviousMonth = { budgetViewModel.previousMonth() },
                onNextMonth = { budgetViewModel.nextMonth() },
                onAssignBudget = { catId, amount -> budgetViewModel.assignBudget(catId, amount) },
                onRefresh = { budgetViewModel.loadBudget() },
                onClearAction = { budgetViewModel.clearActionState() }
            )
        }

        // Transactions
        composable("transactions") {
            val transactionsViewModel: TransactionsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return TransactionsViewModel(transactionRepository) as T
                    }
                }
            )
            val state by transactionsViewModel.state.collectAsState()
            val actionState by transactionsViewModel.actionState.collectAsState()
            val filters by transactionsViewModel.filters.collectAsState()
            LaunchedEffect(Unit) { transactionsViewModel.loadTransactions() }

            TransactionsScreen(
                state = state,
                actionState = actionState,
                searchQuery = filters.search ?: "",
                onSearchChange = { transactionsViewModel.updateSearch(it) },
                onRefresh = { transactionsViewModel.loadTransactions() },
                onCreate = { transactionsViewModel.createTransaction(it) },
                onUpdate = { id, req -> transactionsViewModel.updateTransaction(id, req) },
                onDelete = { transactionsViewModel.deleteTransaction(it) },
                onNextPage = { transactionsViewModel.nextPage() },
                onPreviousPage = { transactionsViewModel.previousPage() },
                onClearAction = { transactionsViewModel.clearActionState() }
            )
        }

        // Accounts
        composable("accounts") {
            val accountsViewModel: AccountsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return AccountsViewModel(accountRepository) as T
                    }
                }
            )
            val state by accountsViewModel.state.collectAsState()
            val actionState by accountsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { accountsViewModel.loadAccounts() }

            AccountsScreen(
                state = state,
                actionState = actionState,
                onRefresh = { accountsViewModel.loadAccounts() },
                onCreate = { name, type, balance, onBudget ->
                    accountsViewModel.createAccount(name, type, balance, onBudget)
                },
                onUpdate = { id, req -> accountsViewModel.updateAccount(id, req) },
                onDelete = { accountsViewModel.deleteAccount(it) },
                onClearAction = { accountsViewModel.clearActionState() }
            )
        }

        // More
        composable("more") {
            MoreScreen(navController)
        }

        // Reports
        composable("reports") {
            val reportsViewModel: ReportsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ReportsViewModel(reportRepository) as T
                    }
                }
            )
            val state by reportsViewModel.state.collectAsState()
            val selectedTab by reportsViewModel.selectedTab.collectAsState()
            LaunchedEffect(Unit) { reportsViewModel.loadReport(0) }

            ReportsScreen(
                state = state,
                selectedTab = selectedTab,
                onSelectTab = { reportsViewModel.selectTab(it) },
                onRefresh = { reportsViewModel.loadReport(selectedTab) }
            )
        }

        // Subscriptions
        composable("subscriptions") {
            val subscriptionsViewModel: SubscriptionsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SubscriptionsViewModel(subscriptionRepository) as T
                    }
                }
            )
            val state by subscriptionsViewModel.state.collectAsState()
            val actionState by subscriptionsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { subscriptionsViewModel.loadSubscriptions() }

            SubscriptionsScreen(
                state = state,
                actionState = actionState,
                onRefresh = { subscriptionsViewModel.loadSubscriptions() },
                onCreate = { subscriptionsViewModel.createSubscription(it) },
                onUpdate = { id, req -> subscriptionsViewModel.updateSubscription(id, req) },
                onDelete = { subscriptionsViewModel.deleteSubscription(it) },
                onClearAction = { subscriptionsViewModel.clearActionState() }
            )
        }

        // Goals
        composable("goals") {
            val goalsViewModel: GoalsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return GoalsViewModel(goalRepository) as T
                    }
                }
            )
            val state by goalsViewModel.state.collectAsState()
            val actionState by goalsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { goalsViewModel.loadGoals() }

            GoalsScreen(
                state = state,
                actionState = actionState,
                onRefresh = { goalsViewModel.loadGoals() },
                onCreate = { goalsViewModel.createGoal(it) },
                onUpdate = { id, req -> goalsViewModel.updateGoal(id, req) },
                onDelete = { goalsViewModel.deleteGoal(it) },
                onContribute = { id, amount -> goalsViewModel.contribute(id, amount) },
                onClearAction = { goalsViewModel.clearActionState() }
            )
        }

        // Debts
        composable("debts") {
            val debtsViewModel: DebtsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return DebtsViewModel(debtRepository) as T
                    }
                }
            )
            val state by debtsViewModel.state.collectAsState()
            val actionState by debtsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { debtsViewModel.loadDebts() }

            DebtsScreen(
                state = state,
                actionState = actionState,
                onRefresh = { debtsViewModel.loadDebts() },
                onCreate = { debtsViewModel.createDebt(it) },
                onUpdate = { id, req -> debtsViewModel.updateDebt(id, req) },
                onDelete = { debtsViewModel.deleteDebt(it) },
                onClearAction = { debtsViewModel.clearActionState() }
            )
        }

        // Investments
        composable("investments") {
            val investmentsViewModel: InvestmentsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return InvestmentsViewModel(investmentRepository) as T
                    }
                }
            )
            val state by investmentsViewModel.state.collectAsState()
            val actionState by investmentsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { investmentsViewModel.loadInvestments() }

            InvestmentsScreen(
                state = state,
                actionState = actionState,
                onRefresh = { investmentsViewModel.loadInvestments() },
                onCreate = { investmentsViewModel.createInvestment(it) },
                onUpdate = { id, req -> investmentsViewModel.updateInvestment(id, req) },
                onDelete = { investmentsViewModel.deleteInvestment(it) },
                onClearAction = { investmentsViewModel.clearActionState() }
            )
        }

        // Settings
        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(settingsRepository) as T
                    }
                }
            )
            val state by settingsViewModel.state.collectAsState()
            val actionState by settingsViewModel.actionState.collectAsState()
            LaunchedEffect(Unit) { settingsViewModel.loadSettings() }

            SettingsScreen(
                state = state,
                actionState = actionState,
                onUpdateSettings = { settingsViewModel.updateSettings(it) },
                onUpdateServerUrl = { settingsViewModel.updateServerUrl(it) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRefresh = { settingsViewModel.loadSettings() },
                onClearAction = { settingsViewModel.clearActionState() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavHostController) {
    data class MenuItem(val title: String, val route: String, val icon: ImageVector)

    val menuItems = listOf(
        MenuItem("Reports", "reports", Icons.Default.Assessment),
        MenuItem("Subscriptions", "subscriptions", Icons.Default.Repeat),
        MenuItem("Goals", "goals", Icons.Default.Flag),
        MenuItem("Debts", "debts", Icons.Default.CreditCard),
        MenuItem("Investments", "investments", Icons.Default.TrendingUp),
        MenuItem("Settings", "settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(menuItems.size) { index ->
                val item = menuItems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    onClick = { navController.navigate(item.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
