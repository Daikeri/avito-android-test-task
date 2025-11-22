package com.example.bookslist

import com.example.util.ResultState


sealed class MetaBookError {
    data object NetworkError : MetaBookError()
    data object PermissionDenied : MetaBookError()
    data object SaveFailed : MetaBookError()
    data object FileNotFound : MetaBookError()
    data class Unknown(val message: String?) : MetaBookError()
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val storagePath: String, // Путь к файлу в облаке (ключ S3)
    val localPath: String?,  // Путь к файлу на устройстве, если скачан
    val isDownloaded: Boolean
)

interface BookRepository {
    suspend fun getBooks(): ResultState<List<Book>, MetaBookError>

    suspend fun downloadBook(book: Book): ResultState<Book, MetaBookError>

    suspend fun deleteBook(book: Book): ResultState<Book, MetaBookError>
}


