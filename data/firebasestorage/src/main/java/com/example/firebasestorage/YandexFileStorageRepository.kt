package com.example.firebasestorage

import android.content.Context
import android.net.Uri
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.example.books.RawBookRepository
import com.example.books.RawBookError
import com.example.util.ResultState
import java.util.UUID
import javax.inject.Inject
import aws.smithy.kotlin.runtime.net.url.Url
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException

private const val YC_ENDPOINT = "https://storage.yandexcloud.net"
private const val BUCKET_NAME = "daikeri-bucket"
private const val ACCESS_KEY_ID = BuildConfig.YC_ACCESS_KEY_ID
private const val SECRET_KEY = BuildConfig.YC_SECRET_KEY
private const val YC_REGION = "ru-central-1"


class YandexRawBookRepository @Inject constructor(
    @ApplicationContext
    private val appContext: Context
) : RawBookRepository {

    private val s3Client: S3Client = S3Client {

        endpointUrl = Url.parse(YC_ENDPOINT)

        region = YC_REGION

        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = ACCESS_KEY_ID
            secretAccessKey = SECRET_KEY
        }
    }

    override suspend fun uploadFile(uri: Uri): ResultState<String, RawBookError> {

        val objectKey = "uploads/${UUID.randomUUID()}_${getFileName(uri)}"
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

            val response = s3Client.putObject(request)

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