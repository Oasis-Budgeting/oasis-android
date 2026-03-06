package com.oasis.budgeting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.budgeting.data.model.AuthResponse
import com.oasis.budgeting.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun checkAuth() {
        viewModelScope.launch {
            _isLoggedIn.value = repository.isLoggedIn()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            repository.login(email, password)
                .onSuccess {
                    _loginState.value = UiState.Success(it)
                    _isLoggedIn.value = true
                }
                .onFailure {
                    _loginState.value = UiState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun register(name: String, username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            repository.register(name, username, email, password)
                .onSuccess {
                    _registerState.value = UiState.Success(it)
                    _isLoggedIn.value = true
                }
                .onFailure {
                    _registerState.value = UiState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _isLoggedIn.value = false
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
        }
    }

    fun clearLoginState() {
        _loginState.value = UiState.Idle
    }

    fun clearRegisterState() {
        _registerState.value = UiState.Idle
    }
}
