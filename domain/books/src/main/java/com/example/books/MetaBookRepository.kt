package com.example.books

import com.example.util.ResultState

interface MetaBookRepository {

    suspend fun uploadMeta(
        userId: String,
        title: String,
        author: String,
        fileUrl: String
    ): ResultState<Unit, MetaBookError>
}

sealed class MetaBookError {
    data object SaveFailed : MetaBookError()
    data object NetworkError : MetaBookError()
    data class Unknown(val message: String?) : MetaBookError()
}