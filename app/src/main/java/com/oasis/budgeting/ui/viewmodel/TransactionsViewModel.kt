package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionFilters(
    val accountId: Int? = null,
    val categoryId: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val search: String? = null
)

class TransactionsViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TransactionsResponse>>(UiState.Loading)
    val state: StateFlow<UiState<TransactionsResponse>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    private val _filters = MutableStateFlow(TransactionFilters())
    val filters: StateFlow<TransactionFilters> = _filters.asStateFlow()

    private var currentPage = 1

    fun loadTransactions(page: Int = 1) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            currentPage = page
            val f = _filters.value
            repository.getTransactions(
                accountId = f.accountId,
                categoryId = f.categoryId,
                startDate = f.startDate,
                endDate = f.endDate,
                search = f.search,
                page = page,
                limit = 50
            )
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load transactions") }
        }
    }

    fun updateFilters(filters: TransactionFilters) {
        _filters.value = filters
        loadTransactions(1)
    }

    fun updateSearch(query: String) {
        _filters.value = _filters.value.copy(search = query.ifBlank { null })
        loadTransactions(1)
    }

    fun createTransaction(request: CreateTransactionRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createTransaction(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Transaction created")
                    loadTransactions(currentPage)
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to create transaction") }
        }
    }

    fun updateTransaction(id: Int, request: UpdateTransactionRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateTransaction(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Transaction updated")
                    loadTransactions(currentPage)
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update transaction") }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteTransaction(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Transaction deleted")
                    loadTransactions(currentPage)
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete transaction") }
        }
    }

    fun nextPage() {
        val current = (_state.value as? UiState.Success)?.data?.pagination
        if (current != null && currentPage < current.totalPages) {
            loadTransactions(currentPage + 1)
        }
    }

    fun previousPage() {
        if (currentPage > 1) {
            loadTransactions(currentPage - 1)
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
