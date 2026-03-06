package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.SettingsResponse
import com.oasis.budgeting.data.model.SettingsUpdateRequest
import com.oasis.budgeting.data.remote.RetrofitClient
import com.oasis.budgeting.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppSettings(
    val settings: SettingsResponse = SettingsResponse(),
    val serverUrl: String = ""
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<AppSettings>>(UiState.Loading)
    val state: StateFlow<UiState<AppSettings>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val serverUrl = repository.getServerUrl()
            repository.getSettings()
                .onSuccess {
                    _state.value = UiState.Success(AppSettings(it, serverUrl))
                }
                .onFailure {
                    _state.value = UiState.Error(it.message ?: "Failed to load settings")
                }
        }
    }

    fun updateSettings(request: SettingsUpdateRequest) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateSettings(request)
                .onSuccess {
                    _actionState.value = UiState.Success("Settings updated")
                    loadSettings()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update settings") }
        }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateServerUrl(url)
            RetrofitClient.resetInstance()
            _actionState.value = UiState.Success("Server URL updated")
            loadSettings()
        }
    }

    fun clearActionState() {
        _actionState.value = UiState.Idle
    }
}
