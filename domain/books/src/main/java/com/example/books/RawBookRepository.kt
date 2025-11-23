package com.example.books

import android.net.Uri
import com.example.util.ResultState

interface RawBookRepository {

    suspend fun uploadFile(uri: Uri, fileName: String): ResultState<String, RawBookError>
}

sealed class RawBookError {
    data object FileReadError : RawBookError()
    data object UploadFailed : RawBookError()
    data object NetworkError : RawBookError()
    data class Unknown(val message: String?) : RawBookError()
}