package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.InvestmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvestmentsViewModel(private val repository: InvestmentRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Investment>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Investment>>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadInvestments() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getInvestments()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load investments") }
        }
    }

    fun createInvestment(request: CreateInvestmentRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createInvestment(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Investment added")
                    loadInvestments()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to add investment") }
        }
    }

    fun updateInvestment(id: Int, request: UpdateInvestmentRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateInvestment(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Investment updated")
                    loadInvestments()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update investment") }
        }
    }

    fun deleteInvestment(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteInvestment(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Investment deleted")
                    loadInvestments()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete investment") }
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
