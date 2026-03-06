package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.BudgetResponse
import com.oasis.budgeting.data.model.BudgetSummary
import com.oasis.budgeting.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BudgetData(
    val budget: BudgetResponse? = null,
    val summary: BudgetSummary? = null
)

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<BudgetData>>(UiState.Loading)
    val state: StateFlow<UiState<BudgetData>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    private val _currentMonth = MutableStateFlow(
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    )
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    fun loadBudget(month: String? = null) {
        val m = month ?: _currentMonth.value
        _currentMonth.value = m
        viewModelScope.launch {
            _state.value = UiState.Loading
            var budget: BudgetResponse? = null
            var summary: BudgetSummary? = null

            repository.getBudget(m).onSuccess { budget = it }
            repository.getSummary(m).onSuccess { summary = it }

            if (budget != null || summary != null) {
                _state.value = UiState.Success(BudgetData(budget, summary))
            } else {
                _state.value = UiState.Error("Failed to load budget")
            }
        }
    }

    fun assignBudget(categoryId: Int, assigned: Double) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.assignBudget(_currentMonth.value, categoryId, assigned)
                .onSuccess {
                    _actionState.value = UiState.Success("Budget assigned")
                    loadBudget()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to assign budget") }
        }
    }

    fun previousMonth() {
        val current = LocalDate.parse("${_currentMonth.value}-01")
        val prev = current.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
        loadBudget(prev)
    }

    fun nextMonth() {
        val current = LocalDate.parse("${_currentMonth.value}-01")
        val next = current.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
        loadBudget(next)
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
