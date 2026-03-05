package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.Account
import com.oasis.budgeting.data.model.UpdateAccountRequest
import com.oasis.budgeting.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountsViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Account>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Account>>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadAccounts() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getAccounts()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load accounts") }
        }
    }

    fun createAccount(name: String, type: String, balance: Double, onBudget: Boolean = true) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createAccount(name, type, balance, onBudget)
                .onSuccess {
                    _actionState.value = UiState.Success("Account created")
                    loadAccounts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to create account") }
        }
    }

    fun updateAccount(id: Int, request: UpdateAccountRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateAccount(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Account updated")
                    loadAccounts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update account") }
        }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteAccount(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Account deleted")
                    loadAccounts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete account") }
        }
    }

    fun reconcileAccount(id: Int, statementBalance: Double, statementDate: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.reconcileAccount(id, statementBalance, statementDate)
                .onSuccess {
                    _actionState.value = UiState.Success("Account reconciled")
                    loadAccounts()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to reconcile") }
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
