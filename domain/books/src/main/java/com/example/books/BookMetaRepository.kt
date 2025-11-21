package com.example.books

interface BookMetaRepository {

    suspend fun saveBookMeta(
        userId: String,
        title: String,
        author: String,
        fileUrl: String
    )
}