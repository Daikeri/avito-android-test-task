package com.example.userprofile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: ProfileError? = null,
    val isEditing: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null
)