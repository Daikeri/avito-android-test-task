package com.example.books

import android.net.Uri
import com.example.util.ResultState
import javax.inject.Inject


class UploadBookUseCase @Inject constructor(
    private val fileStorageRepository: FileStorageRepository,
    private val bookMetaRepository: BookMetaRepository
) {
    suspend operator fun invoke(
        userId: String,
        title: String,
        author: String,
        fileUri: Uri
    ): ResultState<String> {
        return try {

            val downloadUrl = fileStorageRepository.uploadFile(fileUri)

//            bookMetaRepository.saveBookMeta(
//                userId = userId,
//                title = title,
//                author = author,
//                fileUrl = downloadUrl
//            )

            ResultState.Success("Книга успешно сохранена")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}