package com.example.firebasefirestore

import com.example.books.MetaBookRepository
import com.example.books.MetaBookError
import com.example.util.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirestoreMetaBookRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MetaBookRepository {


    override suspend fun uploadMeta(
        userId: String,
        title: String,
        author: String,
        fileUrl: String
    ): ResultState<Unit, MetaBookError> {
        return try {
            val book = BookDocument(
                title = title,
                author = author,
                fileUrl = fileUrl,
                userId = userId
            )

            firestore.collection("books")
                .add(book)
                .await()

            ResultState.Success(Unit)
        } catch (e: Exception) {

            ResultState.Error(MetaBookError.SaveFailed)
        }
    }
}