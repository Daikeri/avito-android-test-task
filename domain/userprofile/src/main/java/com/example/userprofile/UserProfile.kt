package com.example.userprofile

data class UserProfile(
    val userId: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val photoUrl: String?
)