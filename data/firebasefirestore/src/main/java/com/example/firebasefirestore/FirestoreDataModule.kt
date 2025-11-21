package com.example.firebasefirestore

import com.example.books.BookMetaRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreDataModule {
    @Binds
    @Singleton
    abstract fun bindBookMetaRepository(impl: FirestoreBookRepository): BookMetaRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseProvidersModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}