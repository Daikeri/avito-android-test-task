package com.example.auth

import com.example.util.ResultState
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: UserProfileRepository
) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): ResultState<Unit, RegisterError> {

        when (val reg = authRepository.register(email, password)) {
            is ResultState.Error -> {
                return ResultState.Error(mapAuthError(reg.error))
            }
            is ResultState.Success -> Unit
        }

        val uid = authRepository.getCurrentUserId()
            ?: return ResultState.Error(RegisterError.Unknown("UID не получен"))

        when (val profile = profileRepository.createUserProfile(uid, firstName, lastName)) {
            is ResultState.Error -> {
                return ResultState.Error(RegisterError.ProfileSaveFailed(profile.error))
            }
            is ResultState.Success -> Unit
        }

        return ResultState.Success(Unit)
    }

    private fun mapAuthError(error: AuthError): RegisterError {
        return when (error) {
            AuthError.UserCollision -> RegisterError.UserAlreadyExists
            AuthError.WeakPassword -> RegisterError.WeakPassword
            AuthError.Network -> RegisterError.Network
            else -> RegisterError.Unknown(error.toString())
        }
    }
}

sealed class RegisterError {
    data object UserAlreadyExists : RegisterError()
    data object WeakPassword : RegisterError()
    data object Network : RegisterError()
    data class ProfileSaveFailed(val raw: Throwable) : RegisterError()
    data class Unknown(val msg: String) : RegisterError()
}
