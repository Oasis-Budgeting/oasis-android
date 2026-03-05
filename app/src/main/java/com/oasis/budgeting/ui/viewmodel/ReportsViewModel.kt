package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReportsData(
    val spendingByCategory: List<SpendingByCategory> = emptyList(),
    val incomeVsExpense: List<IncomeVsExpense> = emptyList(),
    val netWorth: List<NetWorth> = emptyList(),
    val budgetVsActual: List<BudgetVsActual> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList()
)

class ReportsViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ReportsData>>(UiState.Loading)
    val state: StateFlow<UiState<ReportsData>> = _state.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
        loadReport(index)
    }

    fun loadReport(tabIndex: Int = 0) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val data = (_state.value as? UiState.Success)?.data ?: ReportsData()

            when (tabIndex) {
                0 -> {
                    repository.getSpendingByCategory()
                        .onSuccess { _state.value = UiState.Success(data.copy(spendingByCategory = it)) }
                        .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load report") }
                }
                1 -> {
                    repository.getIncomeVsExpense(12)
                        .onSuccess { _state.value = UiState.Success(data.copy(incomeVsExpense = it)) }
                        .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load report") }
                }
                2 -> {
                    repository.getNetWorth(12)
                        .onSuccess { _state.value = UiState.Success(data.copy(netWorth = it)) }
                        .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load report") }
                }
                3 -> {
                    repository.getBudgetVsActual(currentMonth)
                        .onSuccess { _state.value = UiState.Success(data.copy(budgetVsActual = it)) }
                        .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load report") }
                }
            }
        }
    }
}
