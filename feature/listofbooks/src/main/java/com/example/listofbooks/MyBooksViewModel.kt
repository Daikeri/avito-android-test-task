package com.example.listofbooks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookslist.Book
import com.example.bookslist.DeleteBookUseCase
import com.example.bookslist.DownloadBookUseCase
import com.example.bookslist.GetBooksUseCase
import com.example.bookslist.MetaBookError
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MyBooksUiState {
    data object Loading : MyBooksUiState
    data class Success(val books: List<Book>, val filteredBooks: List<Book>) : MyBooksUiState
    data object Empty : MyBooksUiState
    data class Error(val message: String) : MyBooksUiState
}

sealed interface MyBooksEvent {
    data class ShowToast(val message: String) : MyBooksEvent
}

@HiltViewModel
class MyBooksViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val downloadBookUseCase: DownloadBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyBooksUiState>(MyBooksUiState.Loading)
    val uiState: StateFlow<MyBooksUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _events = MutableSharedFlow<MyBooksEvent>()
    val events = _events.asSharedFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = MyBooksUiState.Loading

            when (val result = getBooksUseCase()) {
                is ResultState.Success -> {
                    val books = result.data
                    if (books.isEmpty()) {
                        _uiState.value = MyBooksUiState.Empty
                    } else {
                        _uiState.value = MyBooksUiState.Success(
                            books,
                            filterBooks(books, _searchQuery.value)
                        )
                    }
                }
                is ResultState.Error -> {
                    val errorMessage = mapErrorToString(result.error)
                    _uiState.value = MyBooksUiState.Error(errorMessage)
                }
            }
        }
    }

    fun onBookAction(book: Book) {
        viewModelScope.launch {
            if (book.isDownloaded) {
                deleteBook(book)
            } else {
                downloadBook(book)
            }
        }
    }

    private suspend fun downloadBook(book: Book) {
        Log.e("download book", "$book")
        when (val result = downloadBookUseCase(book)) {
            is ResultState.Success -> {
                updateBookInList(result.data)
                _events.emit(MyBooksEvent.ShowToast("Книга загружена"))
            }
            is ResultState.Error -> {
                Log.e("ERROR FROM VM", "${result.error}")
                _events.emit(MyBooksEvent.ShowToast("Ошибка: ${mapErrorToString(result.error)}"))
            }
        }
    }

    private suspend fun deleteBook(book: Book) {
        when (val result = deleteBookUseCase(book)) {
            is ResultState.Success -> {
                updateBookInList(result.data)
                _events.emit(MyBooksEvent.ShowToast("Файл удалён"))
            }
            is ResultState.Error -> {
                _events.emit(MyBooksEvent.ShowToast("Ошибка: ${mapErrorToString(result.error)}"))
            }
        }
    }

    private fun mapErrorToString(error: MetaBookError): String {
        return when (error) {
            is MetaBookError.NetworkError -> "Проверьте соединение с интернетом"
            is MetaBookError.PermissionDenied -> "Нет прав доступа к файлам"
            is MetaBookError.SaveFailed -> "Не удалось сохранить файл"
            is MetaBookError.FileNotFound -> "Файл не найден"
            is MetaBookError.Unknown -> error.message ?: "Неизвестная ошибка"
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val currentState = _uiState.value
        if (currentState is MyBooksUiState.Success) {
            _uiState.update {
                currentState.copy(filteredBooks = filterBooks(currentState.books, query))
            }
        }
    }

    private fun filterBooks(books: List<Book>, query: String): List<Book> {
        if (query.isBlank()) return books
        return books.filter {
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }
    }

    private fun updateBookInList(updatedBook: Book) {
        val currentState = _uiState.value
        if (currentState is MyBooksUiState.Success) {
            val newBooks = currentState.books.map { if (it.id == updatedBook.id) updatedBook else it }
            _uiState.update {
                currentState.copy(books = newBooks, filteredBooks = filterBooks(newBooks, _searchQuery.value))
            }
        }
    }
}