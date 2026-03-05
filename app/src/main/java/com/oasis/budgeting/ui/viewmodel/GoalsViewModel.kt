package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.*
import com.oasis.budgeting.data.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(private val repository: GoalRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Goal>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Goal>>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadGoals() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getGoals()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Failed to load goals") }
        }
    }

    fun createGoal(request: CreateGoalRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.createGoal(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Goal created")
                    loadGoals()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to create goal") }
        }
    }

    fun updateGoal(id: Int, request: UpdateGoalRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateGoal(id, request)
                .onSuccess {
                    _actionState.value = UiState.Success("Goal updated")
                    loadGoals()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update goal") }
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.deleteGoal(id)
                .onSuccess {
                    _actionState.value = UiState.Success("Goal deleted")
                    loadGoals()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to delete goal") }
        }
    }

    fun contribute(id: Int, amount: Double) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.contribute(id, amount)
                .onSuccess {
                    _actionState.value = UiState.Success("Contribution added")
                    loadGoals()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to contribute") }
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
