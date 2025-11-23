package com.example.userprofile

import com.example.firebasefirestore.FirestoreRds
import com.example.util.ResultState
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreImageCollRepositoryImpl @Inject constructor(
    private val rds: FirestoreRds
) : FirestoreImageCollRepository {

    private companion object {
        const val COLLECTION = "image"
    }

    override suspend fun getUserImage(userId: String): ResultState<String?, ProfileError> {
        return try {
            val doc = rds.getInstance()
                .collection(COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) {
                ResultState.Success(null)
            } else {
                val fileUrl = doc.getString("fileUrl")
                ResultState.Success(fileUrl)
            }
        } catch (e: FirebaseFirestoreException) {
            val error = if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                ProfileError.Network
            } else {
                ProfileError.Unknown(e.localizedMessage)
            }
            ResultState.Error(error)
        } catch (e: Exception) {
            ResultState.Error(ProfileError.Unknown(e.localizedMessage))
        }
    }

    override suspend fun setUserImage(
        userId: String,
        fileUrl: String
    ): ResultState<Unit, ProfileError> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "fileUrl" to fileUrl
            )

            rds.getInstance()
                .collection(COLLECTION)
                .document(userId)
                .set(data, SetOptions.merge())
                .await()

            ResultState.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            val error = if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                ProfileError.Network
            } else {
                ProfileError.Unknown(e.localizedMessage)
            }
            ResultState.Error(error)
        } catch (e: Exception) {
            ResultState.Error(ProfileError.Unknown(e.localizedMessage))
        }
    }
}