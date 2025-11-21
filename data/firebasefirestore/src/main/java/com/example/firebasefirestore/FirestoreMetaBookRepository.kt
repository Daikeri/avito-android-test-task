package com.example.firebasefirestore

import com.example.books.MetaBookRepository
import com.example.books.MetaBookError
import com.example.util.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
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

        } catch (e: FirebaseFirestoreException) {
            if (e.code == PERMISSION_DENIED) {
                return ResultState.Error(MetaBookError.PermissionDenied)
            }
            return ResultState.Error(MetaBookError.Unknown(e.message))
        } catch (e: FirebaseNetworkException) {
            return ResultState.Error(MetaBookError.NetworkError)
        } catch (e: Exception) {
            return ResultState.Error(MetaBookError.Unknown(e.localizedMessage))
        }
    }
}