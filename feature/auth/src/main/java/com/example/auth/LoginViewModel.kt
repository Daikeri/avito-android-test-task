package com.example.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginStatus {
    data object Idle : LoginStatus()
    data object Loading : LoginStatus()
    data object Success : LoginStatus()
    data class Error(val message: String) : LoginStatus()
}

data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginStatus: LoginStatus = LoginStatus.Idle
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, passwordError = null) }
    }

    fun resetErrorStatus() {
        if (_uiState.value.loginStatus is LoginStatus.Error) {
            _uiState.update { it.copy(loginStatus = LoginStatus.Idle) }
        }
    }

    fun login() {
        val email = _uiState.value.emailInput
        val password = _uiState.value.passwordInput

        val isEmailValid = email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
        val isPasswordValid = password.length >= 6

        if (!isEmailValid || !isPasswordValid) {
            _uiState.update {
                it.copy(
                    emailError = if (!isEmailValid) "Некорректный email адрес." else null,
                    passwordError = if (!isPasswordValid) "Пароль должен быть не менее 6 символов." else null
                )
            }
            return
        }

        _uiState.update { it.copy(loginStatus = LoginStatus.Loading) }

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is ResultState.Success -> {
                    // Успех (переход в состояние Success)
                    _uiState.update { it.copy(loginStatus = LoginStatus.Success) }
                }
                is ResultState.Error -> {
                    // Ошибка (переход в состояние Error)
                    _uiState.update { it.copy(loginStatus = LoginStatus.Error(result.exceptionMsg)) }
                }
            }
        }
    }
}