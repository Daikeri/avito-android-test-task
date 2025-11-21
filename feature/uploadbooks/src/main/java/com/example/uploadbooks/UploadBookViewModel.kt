package com.example.uploadbooks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthRepository
import com.example.books.UploadBookUseCase
import com.example.books.UploadDomainError
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadUiState(
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val isSuccess: Boolean = false,
    val error: UploadDomainError? = null
)

@HiltViewModel
class UploadBookViewModel @Inject constructor(
    private val uploadBookUseCase: UploadBookUseCase,
    // Обновлен интерфейс репозитория
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    private var uploadJob: Job? = null

    fun uploadBook(title: String, author: String, fileUri: Uri?) {
        if (title.isBlank() || author.isBlank() || fileUri == null) {
            _uiState.update { it.copy(error = UploadDomainError.Unknown("Заполните все поля")) }
            return
        }

        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(error = UploadDomainError.NotAuthorized) }
            return
        }

        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, progress = 0f, error = null, isSuccess = false) }

            val progressJob = launch { simulateProgress() }

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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            progress = 0f,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { UploadUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun getErrorMessage(error: UploadDomainError): String {
        return when (error) {
            UploadDomainError.FileUploadFailed -> "Ошибка загрузки файла. Возможно, проблема с хранилищем."
            UploadDomainError.MetadataSaveFailed -> "Ошибка сохранения метаданных книги."
            UploadDomainError.Network -> "Отсутствует подключение к сети. Проверьте соединение."
            UploadDomainError.NotAuthorized -> "Пользователь не авторизован. Пожалуйста, войдите в аккаунт."
            is UploadDomainError.Unknown -> error.message ?: "Произошла неизвестная ошибка."
        }
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