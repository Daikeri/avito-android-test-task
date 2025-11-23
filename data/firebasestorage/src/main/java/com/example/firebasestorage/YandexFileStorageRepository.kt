package com.example.firebasestorage

import android.content.Context
import android.net.Uri
import android.net.http.HttpEngine
import android.util.Log
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import com.example.books.RawBookRepository
import com.example.books.RawBookError
import com.example.util.ResultState
import java.util.UUID
import javax.inject.Inject
import aws.smithy.kotlin.runtime.net.url.Url
import com.example.yandexcloud.BUCKET_NAME
import com.example.yandexcloud.YC_ENDPOINT
import com.example.yandexcloud.YandexCloudRds
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException


class YandexRawBookRepository @Inject constructor(
    private val yandexCloudRds: YandexCloudRds,
    @ApplicationContext private val appContext: Context
) : RawBookRepository {

    override suspend fun uploadFile(uri: Uri, fileName: String): ResultState<String, RawBookError> {

        val objectKey = "uploads/${UUID.randomUUID()}_${fileName}"
        val contentResolver = appContext.contentResolver

        val tempFile = File(appContext.cacheDir, UUID.randomUUID().toString())

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return ResultState.Error(RawBookError.FileReadError)

            val request = PutObjectRequest {
                bucket = BUCKET_NAME
                key = objectKey

                body = tempFile.asByteStream()
                contentLength = tempFile.length()
            }

            val response = yandexCloudRds.getInstance().putObject(request)

            if (response.eTag != null) {
                val downloadUrl = "$YC_ENDPOINT/$BUCKET_NAME/$objectKey"
                return ResultState.Success(downloadUrl)
            } else {
                return ResultState.Error(RawBookError.UploadFailed)
            }
        } catch (e: IOException) {
            return ResultState.Error(RawBookError.NetworkError)
        } catch (e: Exception) {
            return ResultState.Error(RawBookError.Unknown(e.localizedMessage))
        } finally {
            tempFile.delete()
        }
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment?.split("/")?.lastOrNull() ?: "unknown_file"
    }
}