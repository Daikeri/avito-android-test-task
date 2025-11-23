package com.example.userprofile

import com.example.firebaseauth.FirebaseAuthRds
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val rds: FirebaseAuthRds
) : FirebaseAuthRepository {

    override fun getUserId(): String? = rds.getInstance().currentUser?.uid

    override fun getEmail(): String? = rds.getInstance().currentUser?.email

    override fun getPhone(): String? = rds.getInstance().currentUser?.phoneNumber

    override fun signOut() {
        rds.getInstance().signOut()
    }
}