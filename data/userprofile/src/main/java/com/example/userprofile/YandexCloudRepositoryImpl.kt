package com.example.userprofile

import android.content.Context
import android.net.Uri
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.io.IOException
import com.example.util.ResultState
import com.example.yandexcloud.BUCKET_NAME
import com.example.yandexcloud.YC_ENDPOINT
import com.example.yandexcloud.YandexCloudRds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject


class YandexImageRepository @Inject constructor(
    private val yandexCloudRds: YandexCloudRds,
    @ApplicationContext private val appContext: Context
): ProfileImageRepository {

    override suspend fun uploadImage(
        uri: Uri,
        fileName: String
    ): ResultState<String, ProfileError> {

        val s3Client = yandexCloudRds.getInstance()

        val objectKey = "profile_images/${UUID.randomUUID()}_$fileName"
        val contentResolver = appContext.contentResolver
        val tempFile = File(appContext.cacheDir, UUID.randomUUID().toString())

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
                ?: return ResultState.Error(ProfileError.Unknown("Не удалось прочитать файл изображения"))

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
            }

            return ResultState.Error(ProfileError.UploadFailed)

        } catch (e: IOException) {
            return ResultState.Error(ProfileError.Network)
        } catch (e: Exception) {
            return ResultState.Error(ProfileError.Unknown(e.localizedMessage))
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun downloadImage(fileKey: String): ResultState<ByteArray, ProfileError> =
        withContext(Dispatchers.IO) {

            val tempFile = File(appContext.cacheDir, "img_${UUID.randomUUID()}.tmp")

            try {
                val request = GetObjectRequest {
                    bucket = BUCKET_NAME
                    key = fileKey
                }

                yandexCloudRds.getInstance().getObject(request) { response ->
                    val body = response.body ?: throw Exception("Empty body")
                    body.writeToFile(tempFile)
                }

                val bytes = tempFile.readBytes()
                ResultState.Success(bytes)

            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                ResultState.Error(ProfileError.Unknown(e.message))
            }
        }

}
