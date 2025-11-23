package com.example.firebaseauth

import com.example.auth.AuthError
import com.example.auth.AuthRepository
import com.example.util.ResultState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuthRds: FirebaseAuthRds
): AuthRepository {
    override suspend fun register(email: String, password: String): ResultState<Unit, AuthError> {
        return try {
            firebaseAuthRds.getInstance().createUserWithEmailAndPassword(email, password).await()
            ResultState.Success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            ResultState.Error(AuthError.UserCollision)
        } catch (e: FirebaseAuthWeakPasswordException) {
            ResultState.Error(AuthError.WeakPassword)
        } catch (e: FirebaseNetworkException) {
            ResultState.Error(AuthError.Network)
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage))
        }
    }

    override suspend fun login(email: String, password: String): ResultState<Unit, AuthError> {
        return try {
            firebaseAuthRds.getInstance().signInWithEmailAndPassword(email, password).await()
            ResultState.Success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResultState.Error(AuthError.InvalidCredentials)
        } catch (e: FirebaseNetworkException) {
            ResultState.Error(AuthError.Network)
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage))
        }
    }

    override fun isUserAuth(): ResultState<Boolean, AuthError> {
        return try {
            val user = firebaseAuthRds.getInstance().currentUser
            ResultState.Success(user != null)
        } catch (e: Exception) {
            ResultState.Error(AuthError.Unknown(e.localizedMessage))
        }
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuthRds.getInstance().currentUser?.uid
    }
}

