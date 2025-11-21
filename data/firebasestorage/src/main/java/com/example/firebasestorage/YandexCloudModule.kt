package com.example.firebasestorage

import com.example.books.RawBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface YandexCloudModule {
    @Binds
    @Singleton
    fun bindYandex(impl: YandexRawBookRepository) : RawBookRepository
}