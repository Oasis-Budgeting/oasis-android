package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.AccountRepository
import com.oasis.budgeting.data.repository.BudgetRepository
import com.oasis.budgeting.data.repository.InsightRepository
import com.oasis.budgeting.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardData(
    val accounts: List<Account> = emptyList(),
    val budgetSummary: BudgetSummary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val totalBalance: Double = 0.0
)

class DashboardViewModel(
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val insightRepository: InsightRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

            var accounts: List<Account> = emptyList()
            var budgetSummary: BudgetSummary? = null
            var recentTransactions: List<Transaction> = emptyList()
            var insights: List<Insight> = emptyList()

            accountRepository.getAccounts()
                .onSuccess { accounts = it }

            budgetRepository.getSummary(currentMonth)
                .onSuccess { budgetSummary = it }

            transactionRepository.getTransactions(limit = 5)
                .onSuccess { recentTransactions = it.transactions }

            insightRepository.getInsights()
                .onSuccess { insights = it }

            val totalBalance = accounts.filter { !it.closed }.sumOf { it.balance }

            _state.value = UiState.Success(
                DashboardData(
                    accounts = accounts,
                    budgetSummary = budgetSummary,
                    recentTransactions = recentTransactions,
                    insights = insights,
                    totalBalance = totalBalance
                )
            )
        }
    }
}
