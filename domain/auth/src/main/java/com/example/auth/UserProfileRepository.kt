package com.example.auth

import com.example.util.ResultState

interface UserProfileRepository {
    suspend fun createUserProfile(
        uid: String,
        firstName: String,
        lastName: String
    ): ResultState<Unit, Throwable>
}
