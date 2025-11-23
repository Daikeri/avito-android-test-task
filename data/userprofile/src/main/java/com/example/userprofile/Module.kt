package com.example.userprofile

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl
    ): FirebaseAuthRepository

    @Binds
    @Singleton
    abstract fun bindUserCollRepository(
        impl: FirestoreUserCollRepositoryImpl
    ): FirestoreUserCollRepository

    @Binds
    @Singleton
    abstract fun bindImageCollRepository(
        impl: FirestoreImageCollRepositoryImpl
    ): FirestoreImageCollRepository

    @Binds
    @Singleton
    abstract fun bindProfileImageRepository(
        impl: YandexImageRepository
    ): ProfileImageRepository
}