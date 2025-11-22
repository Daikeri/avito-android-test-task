package com.example.firebasefirestore

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRds @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getInstance(): FirebaseFirestore = firestore
}