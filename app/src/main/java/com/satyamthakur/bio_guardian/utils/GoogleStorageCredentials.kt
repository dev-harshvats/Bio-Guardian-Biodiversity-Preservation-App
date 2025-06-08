package com.satyamthakur.bio_guardian.utils
import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.StorageOptions

fun getStorageCredentials(context: Context): StorageOptions {
    val credentialsStream = context.assets.open("gcs-credentials.json")
    val credentials = GoogleCredentials.fromStream(credentialsStream)

    return StorageOptions.newBuilder()
        .setCredentials(credentials)
        .build()
}