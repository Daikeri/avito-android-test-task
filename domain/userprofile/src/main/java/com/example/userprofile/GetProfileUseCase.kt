package com.example.userprofile

import com.example.util.ResultState
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val auth: FirebaseAuthRepository,
    private val userRepo: FirestoreUserCollRepository,
    private val imageRepo: FirestoreImageCollRepository
) {

    suspend operator fun invoke(): ResultState<UserProfile, ProfileError> {
        val userId = auth.getUserId() ?: return ResultState.Error(ProfileError.NotAuthorized)

        val userData = userRepo.getUserInfo(userId)
        val imageData = imageRepo.getUserImage(userId)

        return when {
            userData is ResultState.Error -> userData
            imageData is ResultState.Error -> imageData
            userData is ResultState.Success && imageData is ResultState.Success -> {
                val base = userData.data.copy(
                    email = auth.getEmail(),
                    phone = auth.getPhone(),
                    photoUrl = imageData.data
                )
                ResultState.Success(base)
            }
            else -> ResultState.Error(ProfileError.Unknown("Unexpected state"))
        }
    }
}