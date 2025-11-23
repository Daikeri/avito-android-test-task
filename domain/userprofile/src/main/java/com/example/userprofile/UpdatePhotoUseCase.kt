package com.example.userprofile

import android.net.Uri
import com.example.util.ResultState
import javax.inject.Inject

class UpdatePhotoUseCase @Inject constructor(
    private val auth: FirebaseAuthRepository,
    private val imageRepo: ProfileImageRepository,
    private val firestoreImageRepo: FirestoreImageCollRepository
) {

    suspend operator fun invoke(uri: Uri): ResultState<String, ProfileError> {
        val userId = auth.getUserId() ?: return ResultState.Error(ProfileError.NotAuthorized)

        val upload = imageRepo.uploadImage(uri, "avatar.jpg")
        if (upload is ResultState.Error) return upload

        val url = (upload as ResultState.Success).data

        return when (val r = firestoreImageRepo.setUserImage(userId, url)) {
            is ResultState.Success -> ResultState.Success(url)
            is ResultState.Error -> r
        }
    }
}