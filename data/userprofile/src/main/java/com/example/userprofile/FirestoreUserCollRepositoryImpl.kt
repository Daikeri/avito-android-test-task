package com.example.userprofile

import com.example.firebasefirestore.FirestoreRds
import com.example.util.ResultState
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserCollRepositoryImpl @Inject constructor(
    private val rds: FirestoreRds
) : FirestoreUserCollRepository {

    private val COLLECTION = "users"

    override suspend fun getUserInfo(userId: String): ResultState<UserProfile, ProfileError> {
        return try {
            val doc = rds.getInstance()
                .collection(COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) {
                ResultState.Error(ProfileError.NotFound)
            } else {
                val firstName = doc.getString("firstName")
                val lastName = doc.getString("lastName")

                ResultState.Success(
                    UserProfile(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        email = null,
                        phone = null,
                        photoUrl = null
                    )
                )
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

    override suspend fun updateUserName(
        userId: String,
        firstName: String,
        lastName: String
    ): ResultState<Unit, ProfileError> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "firstName" to firstName,
                "lastName" to lastName
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