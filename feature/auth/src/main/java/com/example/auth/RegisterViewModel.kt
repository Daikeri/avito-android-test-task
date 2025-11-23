package com.example.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onFieldChanged(field: RegisterField, value: String) {
        _uiState.update {
            when (field) {
                RegisterField.FirstName ->
                    it.copy(firstName = value, firstNameError = null)

                RegisterField.LastName ->
                    it.copy(lastName = value, lastNameError = null)

                RegisterField.Email ->
                    it.copy(email = value, emailError = null)

                RegisterField.Password ->
                    it.copy(password = value, passwordError = null)
            }
        }
    }

    private fun validate(): Boolean {
        val s = _uiState.value
        var ok = true

        var firstNameError: String? = null
        var lastNameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null

        if (s.firstName.isBlank()) {
            firstNameError = "Введите имя"
            ok = false
        }

        if (s.lastName.isBlank()) {
            lastNameError = "Введите фамилию"
            ok = false
        }

        if (!s.email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            emailError = "Некорректный email"
            ok = false
        }

        if (s.password.length < 6) {
            passwordError = "Пароль должен быть не менее 6 символов"
            ok = false
        }

        _uiState.update {
            it.copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return ok
    }

    private fun errorMessage(error: RegisterError): String {
        return when (error) {
            RegisterError.UserAlreadyExists ->
                "Аккаунт с таким email уже существует"

            RegisterError.WeakPassword ->
                "Пароль слишком слабый"

            RegisterError.Network ->
                "Проблемы с сетью. Проверьте подключение."

            is RegisterError.ProfileSaveFailed ->
                "Ошибка при сохранении профиля"

            is RegisterError.Unknown ->
                error.msg
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }


    fun register() {
        if (!validate()) return

        val s = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = registerUserUseCase(
                firstName = s.firstName,
                lastName = s.lastName,
                email = s.email,
                password = s.password
            )

            when (result) {
                is com.example.util.ResultState.Success -> {
                    _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                }

                is com.example.util.ResultState.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage(result.error)
                        )
                    }
                }
            }
        }
    }
}


data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",

    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

enum class RegisterField {
    FirstName,
    LastName,
    Email,
    Password
}
