package com.example.firebaseauth

import com.example.auth.UserProfileRepository
import com.example.firebasefirestore.FirestoreRds
import com.example.util.ResultState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserProfileRepository @Inject constructor(
    private val firestoreRds: FirestoreRds
) : UserProfileRepository {

    override suspend fun createUserProfile(
        uid: String,
        firstName: String,
        lastName: String
    ): ResultState<Unit, Throwable> {
        return try {
            val data = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "userId" to uid
            )

            firestoreRds.getInstance()
                .collection("users")
                .document(uid)
                .set(data)
                .await()

            ResultState.Success(Unit)

        } catch (e: Exception) {
            ResultState.Error(e)
        }
    }
}
