package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionsViewModel(private val repository: SubscriptionRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Subscription>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Subscription>>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadSubscriptions() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getSubscriptions()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load subscriptions") }
        }
    }

    fun createSubscription(request: CreateSubscriptionRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createSubscription(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Subscription created")
                    loadSubscriptions()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to create subscription") }
        }
    }

    fun updateSubscription(id: Int, request: UpdateSubscriptionRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateSubscription(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Subscription updated")
                    loadSubscriptions()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update subscription") }
        }
    }

    fun deleteSubscription(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteSubscription(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Subscription deleted")
                    loadSubscriptions()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete subscription") }
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
