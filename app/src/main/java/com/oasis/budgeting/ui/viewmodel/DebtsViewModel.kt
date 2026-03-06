package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DebtsData(
    val debts: List<Debt> = emptyList(),
    val strategies: DebtStrategies? = null
)

class DebtsViewModel(private val repository: DebtRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<DebtsData>>(UiState.Loading)
    val state: StateFlow<UiState<DebtsData>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadDebts() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            var debts: List<Debt> = emptyList()
            var strategies: DebtStrategies? = null
            var debtsLoaded = false

            repository.getDebts()
                .onSuccess { debts = it; debtsLoaded = true }
            repository.getStrategies().onSuccess { strategies = it }

            if (!debtsLoaded) {
                _state.value = UiState.Error("Failed to load debts")
                return@launch
            }

            _state.value = UiState.Success(DebtsData(debts, strategies))
        }
    }

    fun createDebt(request: CreateDebtRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createDebt(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Debt added")
                    loadDebts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to create debt") }
        }
    }

    fun updateDebt(id: Int, request: UpdateDebtRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateDebt(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Debt updated")
                    loadDebts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update debt") }
        }
    }

    fun deleteDebt(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteDebt(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Debt deleted")
                    loadDebts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete debt") }
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
