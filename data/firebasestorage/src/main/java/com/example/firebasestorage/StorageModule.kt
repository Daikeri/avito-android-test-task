package com.example.firebasestorage

import com.example.books.FileStorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Yandex

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {
    @Binds
    @Singleton
    fun bindYandex(impl:YandexFileStorageRepository) : FileStorageRepository
}