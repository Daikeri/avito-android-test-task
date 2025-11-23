package com.example.firebaseauth

import com.example.auth.AuthRepository
import com.example.auth.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthModuleProvide {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseAuthModuleBind {
    @Binds
    @Singleton
    fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    fun bindUserProfileRepository(
        impl: FirestoreUserProfileRepository
    ): UserProfileRepository
}

