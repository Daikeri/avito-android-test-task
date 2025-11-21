package com.example.books

import android.net.Uri

interface FileStorageRepository {

    suspend fun uploadFile(uri: Uri): String
}