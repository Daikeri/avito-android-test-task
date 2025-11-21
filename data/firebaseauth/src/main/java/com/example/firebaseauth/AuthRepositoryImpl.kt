package com.example.firebaseauth

import com.example.auth.AuthStatusRepository
import com.example.util.ResultState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): AuthStatusRepository {
    override suspend fun register(email: String, password: String): ResultState<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            ResultState.Success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            ResultState.Error(AuthError.UserCollision.toMessage())
        } catch (e: FirebaseAuthWeakPasswordException) {
            ResultState.Error(AuthError.WeakPassword.toMessage())
        } catch (e: FirebaseNetworkException) {
            ResultState.Error(AuthError.Network.toMessage())
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage).toMessage())
        }
    }

    override suspend fun login(email: String, password: String): ResultState<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            ResultState.Success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResultState.Error(AuthError.InvalidCredentials.toMessage())
        } catch (e: FirebaseNetworkException) {
            ResultState.Error(AuthError.Network.toMessage())
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage).toMessage())
        }
    }

    override fun getCurrentUserStatus(): ResultState<Boolean> {
        return try {
            val user = firebaseAuth.currentUser
            ResultState.Success(user != null)
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage).toMessage())
        }
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}

sealed class AuthError {
    data object Network : AuthError()
    data object UserCollision : AuthError()
    data object WeakPassword : AuthError()
    data object InvalidCredentials : AuthError()
    data class Unknown(val message: String?) : AuthError()

    fun toMessage(): String {
        return when (this) {
            Network -> "Отсутствует подключение к сети. Проверьте соединение."
            UserCollision -> "Пользователь с таким email уже зарегистрирован."
            WeakPassword -> "Пароль слишком слабый. Используйте не менее 6 символов."
            InvalidCredentials -> "Неверный Email или пароль."
            is Unknown -> this.message ?: "Произошла неизвестная ошибка."
        }
    }
}
