package com.example.userprofile

import android.net.Uri
import com.example.util.ResultState

interface FirestoreUserCollRepository {
    suspend fun getUserInfo(userId: String): ResultState<UserProfile, ProfileError>

    suspend fun updateUserName(
        userId: String,
        firstName: String,
        lastName: String
    ): ResultState<Unit, ProfileError>
}

interface FirestoreImageCollRepository {
    suspend fun getUserImage(userId: String): ResultState<String?, ProfileError>
    suspend fun setUserImage(userId: String, fileUrl: String): ResultState<Unit, ProfileError>
}

interface FirebaseAuthRepository {
    fun getUserId(): String?
    fun getEmail(): String?
    fun getPhone(): String?
    fun signOut()
}

interface ProfileImageRepository {
    suspend fun uploadImage(uri: Uri, fileName: String): ResultState<String, ProfileError>
    suspend fun downloadImage(fileKey: String): ResultState<ByteArray, ProfileError>
}