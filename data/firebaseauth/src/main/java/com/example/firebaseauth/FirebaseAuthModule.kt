package com.example.firebaseauth

import com.example.auth.AuthStatusRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
interface AuthModule {
    @Binds
    @Singleton
    fun bindAuthStatusRepository(impl: AuthRepositoryImpl): AuthStatusRepository
}