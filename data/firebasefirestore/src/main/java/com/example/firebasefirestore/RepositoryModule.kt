package com.example.firebasefirestore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Биндим интерфейс к реализации.
    // Если появится RealFileStorage, просто меняем этот метод.
    @Binds
    @Singleton
    abstract fun bindFileStorage(
        mockFileStorage: MockFileStorage
    ): FileStorage

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository
}