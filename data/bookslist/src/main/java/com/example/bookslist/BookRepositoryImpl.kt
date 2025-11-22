package com.example.bookslist

import android.content.Context
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
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

            val books = snapshot.documents.map { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: "Unknown"
                val author = doc.getString("author") ?: "Unknown"
                val storagePath = doc.getString("storagePath") ?: ""
                val coverUrl = doc.getString("coverUrl")

                val localFile = File(booksDir, "$id.epub")
                val isDownloaded = localFile.exists()

                Book(
                    id = id,
                    title = title,
                    author = author,
                    coverUrl = coverUrl,
                    storagePath = storagePath,
                    localPath = if (isDownloaded) localFile.absolutePath else null,
                    isDownloaded = isDownloaded
                )
            }

            ResultState.Success(books)
        } catch (e: Exception) {
            ResultState.Error(mapExceptionToDomainError(e))
        }
    }

    override suspend fun downloadBook(book: Book): ResultState<Book, MetaBookError> = withContext(Dispatchers.IO) {
        val localFile = File(booksDir, "${book.id}.epub")
        try {
            val request = GetObjectRequest {
                bucket = BUCKET_NAME
                key = book.storagePath
            }

            yandexCloudRds.getInstance().getObject(request) { response ->
                val body = response.body ?: throw Exception("Empty body")
                body.writeToFile(localFile)
            }

            ResultState.Success(book.copy(isDownloaded = true, localPath = localFile.absolutePath))
        } catch (e: Exception) {
            if (localFile.exists()) localFile.delete()
            ResultState.Error(mapExceptionToDomainError(e))
        }
    }

    override suspend fun deleteBook(book: Book): ResultState<Book, MetaBookError> = withContext(Dispatchers.IO) {
        try {
            val file = File(book.localPath ?: "")
            if (file.exists() && !file.delete()) {
                return@withContext ResultState.Error(MetaBookError.Unknown("Could not delete file"))
            }
            ResultState.Success(book.copy(isDownloaded = false, localPath = null))
        } catch (e: Exception) {
            ResultState.Error(MetaBookError.Unknown(e.message))
        }
    }

    private fun mapExceptionToDomainError(e: Throwable): MetaBookError {
        return when (e) {
            else -> MetaBookError.Unknown(e.message)
        }
    }
}