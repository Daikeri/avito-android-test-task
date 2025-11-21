package com.example.auth

import com.example.util.ResultState

interface AuthStatusRepository {

    fun getCurrentUserId(): String?

    suspend fun register(email: String, password: String): ResultState<Unit>

    suspend fun login(email: String, password: String): ResultState<Unit>

    fun getCurrentUserStatus(): ResultState<Boolean>
}