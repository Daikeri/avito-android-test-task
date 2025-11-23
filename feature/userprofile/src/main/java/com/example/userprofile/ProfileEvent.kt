package com.example.userprofile

import android.net.Uri

sealed class ProfileEvent {
    object Load : ProfileEvent()
    data class ChangeFirstName(val value: String) : ProfileEvent()
    data class ChangeLastName(val value: String) : ProfileEvent()
    object SaveName : ProfileEvent()
    data class UpdatePhoto(val uri: Uri) : ProfileEvent()
    object SignOut : ProfileEvent()
    object ToggleEdit : ProfileEvent()
    object ErrorConsumed : ProfileEvent()
}