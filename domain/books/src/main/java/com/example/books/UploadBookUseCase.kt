package com.example.books

import android.net.Uri
import com.example.util.ResultState
import javax.inject.Inject

sealed class UploadDomainError {
    data object FileUploadFailed : UploadDomainError()
    data object MetadataSaveFailed : UploadDomainError()
    data object NotAuthorized : UploadDomainError()
    data object Network : UploadDomainError()
    data class Unknown(val message: String?) : UploadDomainError()
}


class UploadBookUseCase @Inject constructor(
    private val rawBookRepository: RawBookRepository,
    private val metaBookRepository: MetaBookRepository
) {
    suspend operator fun invoke(
        userId: String,
        title: String,
        author: String,
        fileUri: Uri,
        fileName: String
    ): ResultState<String, UploadDomainError> {

        val uploadResult = rawBookRepository.uploadFile(fileUri, fileName)

        val downloadUrl = when (uploadResult) {
            is ResultState.Success -> uploadResult.data
            is ResultState.Error -> return ResultState.Error(
                when (uploadResult.error) {
                    is RawBookError.NetworkError -> UploadDomainError.Network
                    else -> UploadDomainError.FileUploadFailed
                }
            )
        }

        val metaResult = metaBookRepository.uploadMeta(
            userId = userId,
            title = title,
            author = author,
            fileUrl = downloadUrl,
        )

        return when (metaResult) {
            is ResultState.Success -> ResultState.Success("Книга успешно сохранена")
            is ResultState.Error -> ResultState.Error(
                when (metaResult.error) {
                    is MetaBookError.NetworkError -> UploadDomainError.Network
                    else -> UploadDomainError.MetadataSaveFailed
                }
            )
        }
    }
}