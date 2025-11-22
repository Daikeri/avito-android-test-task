package com.example.yandexcloud

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.net.url.Url
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val YC_ENDPOINT = "https://storage.yandexcloud.net"
const val BUCKET_NAME = "daikeri-bucket"
const val YC_REGION = "ru-central-1"

@Module
@InstallIn(SingletonComponent::class)
object S3ClientModule {

    @Singleton
    @Provides
    fun provideS3Client(): S3Client {
        return S3Client {
            endpointUrl = Url.parse(YC_ENDPOINT)
            region = YC_REGION

            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = BuildConfig.YC_ACCESS_KEY_ID
                secretAccessKey = BuildConfig.YC_SECRET_KEY
            }
            continueHeaderThresholdBytes = null
        }
    }
}