package com.example.firebasestorage


import android.net.Uri
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

interface FileStorage {
    suspend fun uploadFile(uri: Uri): String
}


class MockFileStorage @Inject constructor() : FileStorage {

    override suspend fun uploadFile(uri: Uri): String {
        // Имитация работы сети (подготовка, хендшейк и т.д.)
        val networkDelay = Random.nextLong(1500, 4000)
        delay(networkDelay)

        // В реальном мире здесь мог быть код:
        // return storageRef.putFile(uri).await().storage.downloadUrl.await().toString()

        // Возвращаем фейковую ссылку
        val fileName = uri.lastPathSegment ?: "unknown_file"
        return "https://fake-storage.com/books/$fileName"
    }
}