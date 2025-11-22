package com.example.bookslist

import javax.inject.Inject


class GetBooksUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke() = repository.getBooks()
}

class DownloadBookUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(book: Book) = repository.downloadBook(book)
}

class DeleteBookUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(book: Book) = repository.deleteBook(book)
}