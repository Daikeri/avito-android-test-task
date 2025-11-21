package com.example.firebasefirestore

data class BookDocument(
    val title: String,
    val author: String,
    val fileUrl: String,
    val userId: String,
    val dateAdded: Long = System.currentTimeMillis()
)