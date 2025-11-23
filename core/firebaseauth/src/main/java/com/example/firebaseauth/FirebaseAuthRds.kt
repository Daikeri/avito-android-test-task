package com.example.firebaseauth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseAuthRds @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun getInstance(): FirebaseAuth = firebaseAuth
}