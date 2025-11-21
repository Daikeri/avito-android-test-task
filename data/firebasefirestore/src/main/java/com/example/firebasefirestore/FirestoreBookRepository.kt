package com.example.firebasefirestore

import com.example.books.BookMetaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreBookRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookMetaRepository {

    override suspend fun saveBookMeta(
        userId: String,
        title: String,
        author: String,
        fileUrl: String
    ) {
        val book = BookDocument(
            title = title,
            author = author,
            fileUrl = fileUrl,
            userId = userId
        )

        firestore.collection("books")
            .add(book)
            .await()
    }
}