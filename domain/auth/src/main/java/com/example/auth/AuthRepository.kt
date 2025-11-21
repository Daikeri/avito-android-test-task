package com.example.auth

import com.example.util.ResultState

interface AuthRepository {

    fun getCurrentUserId(): String?

    suspend fun register(email: String, password: String): ResultState<Unit, AuthError>

    suspend fun login(email: String, password: String): ResultState<Unit, AuthError>

    fun isUserAuth(): ResultState<Boolean, AuthError>
}

sealed class AuthError {
    data object Network : AuthError()
    data object UserCollision : AuthError()
    data object WeakPassword : AuthError()
    data object InvalidCredentials : AuthError()
    data class Unknown(val message: String?) : AuthError()
}