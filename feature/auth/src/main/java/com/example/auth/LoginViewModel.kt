package com.example.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.AuthRepositoryImpl
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- Состояния экрана (СТРОГО 4 СОСТОЯНИЯ ИЗ ТЗ) ---
sealed class LoginStatus {
    data object Idle : LoginStatus()       // 1. Форма доступна для ввода
    data object Loading : LoginStatus()    // 2. Выполняется вход
    data object Success : LoginStatus()    // 3. Успешный вход, переход на следующий экран
    data class Error(val message: String) : LoginStatus() // 4. Ошибка входа
}

// --- Состояние полей ввода ---
data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    // Изначальное состояние - Idle
    val loginStatus: LoginStatus = LoginStatus.Idle
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, passwordError = null) }
    }

    /**
     * Сброс состояния Error обратно в Idle после показа Snackbar.
     */
    fun resetErrorStatus() {
        if (_uiState.value.loginStatus is LoginStatus.Error) {
            _uiState.update { it.copy(loginStatus = LoginStatus.Idle) }
        }
    }

    /**
     * Основная логика входа.
     */
    fun login() {
        val email = _uiState.value.emailInput
        val password = _uiState.value.passwordInput

        // 1. Валидация полей ввода
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

        // 2. Выполнение входа (переход в состояние Loading)
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