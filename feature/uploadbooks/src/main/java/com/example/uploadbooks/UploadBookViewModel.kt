package com.example.uploadbooks


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthStatusRepository
import com.example.books.UploadBookUseCase
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadBookViewModel @Inject constructor(
    private val uploadBookUseCase: UploadBookUseCase,
    private val authStatusRepository: AuthStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    private var uploadJob: Job? = null

    fun uploadBook(title: String, author: String, fileUri: Uri?) {
        if (title.isBlank() || author.isBlank() || fileUri == null) {
            _uiState.update { it.copy(errorMessage = "Заполните все поля") }
            return
        }

        val userId = authStatusRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Ошибка: пользователь не авторизован") }
            return
        }

        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, progress = 0f, errorMessage = null, isSuccess = false) }

            // Запускаем фейковый прогресс-бар для UI
            val progressJob = launch { simulateProgress() }

            // Вызываем UseCase, который координирует работу Storage и Firestore
            val result = uploadBookUseCase(
                userId = userId,
                title = title,
                author = author,
                fileUri = fileUri
            )

            progressJob.cancel()

            when (result) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, progress = 1f, isSuccess = true) }
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, progress = 0f, errorMessage = result.exceptionMsg) }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { UploadUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun simulateProgress() {
        var current = 0f
        while (current < 0.9f) {
            delay(100)
            current += 0.05f
            _uiState.update { it.copy(progress = current) }
        }
    }
}


data class UploadUiState(
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)