package com.example.firebasefirestore

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fileStorage: FileStorage // Инжектим нашу заглушку сюда
) : BookRepository {

    override suspend fun uploadBook(
        userId: String,
        title: String,
        author: String,
        fileUri: Uri
    ): ResultState<String> {
        return try {
            // 1. Сначала грузим файл в (фейковое) хранилище
            val downloadUrl = fileStorage.uploadFile(fileUri)

            // 2. Формируем объект для базы данных
            val book = BookDto(
                title = title,
                author = author,
                fileUrl = downloadUrl,
                userId = userId
            )

            // 3. Сохраняем метаданные в реальный Firestore
            firestore.collection("books")
                .add(book)
                .await()

            ResultState.Success("Книга успешно загружена")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Ошибка при загрузке книги")
        }
    }
}