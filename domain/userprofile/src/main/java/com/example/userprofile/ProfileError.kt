package com.example.userprofile

sealed class ProfileError {
    object Network : ProfileError()
    object NotAuthorized : ProfileError()
    object NotFound : ProfileError()
    data class Unknown(val message: String?) : ProfileError()

    object InvalidName : ProfileError()
    object UploadFailed : ProfileError()
}