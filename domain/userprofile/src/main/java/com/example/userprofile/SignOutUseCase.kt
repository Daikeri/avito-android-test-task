package com.example.userprofile

import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val auth: FirebaseAuthRepository
) {
    operator fun invoke() {
        auth.signOut()
    }
}