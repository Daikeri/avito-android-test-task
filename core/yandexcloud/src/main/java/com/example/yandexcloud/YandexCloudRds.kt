package com.example.yandexcloud

import aws.sdk.kotlin.services.s3.S3Client
import javax.inject.Inject

class YandexCloudRds @Inject constructor(
    private val s3Client: S3Client
) {
    fun getInstance(): S3Client = s3Client
}