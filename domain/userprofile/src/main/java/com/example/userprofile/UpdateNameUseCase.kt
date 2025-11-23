package com.example.userprofile

import com.example.util.ResultState
import javax.inject.Inject

class UpdateNameUseCase @Inject constructor(
    private val auth: FirebaseAuthRepository,
    private val repo: FirestoreUserCollRepository
) {

    suspend operator fun invoke(first: String, last: String): ResultState<Unit, ProfileError> {
        val userId = auth.getUserId() ?: return ResultState.Error(ProfileError.NotAuthorized)

        if (first.isBlank() || last.isBlank()) {
            return ResultState.Error(ProfileError.InvalidName)
        }

        return repo.updateUserName(userId, first, last)
    }
}