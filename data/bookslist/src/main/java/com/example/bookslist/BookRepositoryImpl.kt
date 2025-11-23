package com.example.bookslist

import android.content.Context
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import com.example.bookslist.Book
import com.example.bookslist.BookRepository
import com.example.bookslist.MetaBookError
import com.example.firebasefirestore.FirestoreRds
import com.example.util.ResultState
import com.example.yandexcloud.BUCKET_NAME
import com.example.yandexcloud.YandexCloudRds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val firestoreRds: FirestoreRds,
    private val yandexCloudRds: YandexCloudRds,
    @ApplicationContext private val context: Context
) : BookRepository {

    private val booksDir = File(context.filesDir, "books").apply { mkdirs() }

    override suspend fun getBooks(): ResultState<List<Book>, MetaBookError> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestoreRds.getInstance()
                .collection("books")
                .get()
                .await()

            val books = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val title = doc.getString("title") ?: "Без названия"
                    val author = doc.getString("author") ?: "Неизвестный автор"
                    val fileUrl = doc.getString("fileUrl") ?: return@mapNotNull null
                    val dateAdded = doc.getLong("dateAdded") ?: 0L

                    // Логика парсинга URL и расширения
                    val storageKey = extractKeyFromUrl(fileUrl)
                    val extension = extractExtension(fileUrl)

                    // Проверяем наличие файла с правильным расширением
                    val localFile = File(booksDir, "$id.$extension")
                    val isDownloaded = localFile.exists()

                    Book(
                        id = id,
                        title = title,
                        author = author,
                        fileUrl = fileUrl,
                        storageKey = storageKey,
                        extension = extension,
                        localPath = if (isDownloaded) localFile.absolutePath else null,
                        isDownloaded = isDownloaded,
                        dateAdded = dateAdded
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            ResultState.Success(books)
        } catch (e: Exception) {
            ResultState.Error(MetaBookError.Unknown(e.message))
        }
    }

    override suspend fun downloadBook(book: Book): ResultState<Book, MetaBookError> = withContext(Dispatchers.IO) {
        val localFile = File(booksDir, "${book.id}.${book.extension}")

        try {
            val request = GetObjectRequest {
                bucket = BUCKET_NAME
                key = book.storageKey
            }

            yandexCloudRds.getInstance().getObject(request) { response ->
                val body = response.body ?: throw Exception("Empty body")
                body.writeToFile(localFile)
            }

            ResultState.Success(book.copy(isDownloaded = true, localPath = localFile.absolutePath))
        } catch (e: Exception) {
            if (localFile.exists()) localFile.delete()
            ResultState.Error(MetaBookError.Unknown(e.message))
        }
    }

    override suspend fun deleteBook(book: Book): ResultState<Book, MetaBookError> = withContext(Dispatchers.IO) {
        try {
            val file = File(book.localPath ?: "")
            if (file.exists() && !file.delete()) {
                return@withContext ResultState.Error(MetaBookError.Unknown("Не удалось удалить файл"))
            }
            ResultState.Success(book.copy(isDownloaded = false, localPath = null))
        } catch (e: Exception) {
            ResultState.Error(MetaBookError.Unknown(e.message))
        }
    }

    private fun extractKeyFromUrl(url: String): String {
        return url
            .substringBefore("?")
            .substringAfter("$BUCKET_NAME/")
    }


    private fun extractExtension(url: String): String {
        val key = extractKeyFromUrl(url)
        return key.substringAfterLast('.', "bin")
    }

}