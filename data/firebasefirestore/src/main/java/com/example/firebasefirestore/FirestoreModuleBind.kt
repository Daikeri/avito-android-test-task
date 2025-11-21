package com.example.firebasefirestore


import com.example.books.MetaBookRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModuleProvide {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreModuleBind {
    @Binds
    @Singleton
    abstract fun bindMetaBookRepository(impl: FirestoreMetaBookRepository): MetaBookRepository
}