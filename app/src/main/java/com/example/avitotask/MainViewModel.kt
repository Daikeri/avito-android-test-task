package com.example.avitotask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthRepository // Обновлен импорт
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val startDestination: MainNavGraphDest) : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    // Обновлен интерфейс репозитория
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val result = authRepository.isUserAuth()

            val destination = when (result) {
                is ResultState.Success -> {
                    if (result.data) MainNavGraphDest.Tabs else MainNavGraphDest.Login
                }
                is ResultState.Error -> {
                    MainNavGraphDest.Login
                }
            }

            delay(1000) // для видимости круговой загрузки
            _uiState.value = MainUiState.Success(destination)
        }
    }
}