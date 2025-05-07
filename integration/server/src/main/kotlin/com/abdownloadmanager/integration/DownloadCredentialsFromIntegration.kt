package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class DownloadCredentialsFromIntegration(
    val link: String,
    val headers: Map<String, String>? = null,
    val downloadPage: String? = null,
)

@Serializable
data class AddDownloadOptionsFromIntegration(
    val silentAdd: Boolean = false,
    val silentStart: Boolean = false,
)

@Serializable
data class AddDownloadsFromIntegration(
    val items: List<DownloadCredentialsFromIntegration>,
    val options: AddDownloadOptionsFromIntegration = AddDownloadOptionsFromIntegration()
) {
    companion object {
        fun createFromRequest(json: Json, jsonData: String): AddDownloadsFromIntegration {
            return try {
                json.decodeFromString<AddDownloadsFromIntegration>(jsonData)
            } catch (e: SerializationException) {
                // TODO remove it after a while!!!
                AddDownloadsFromIntegration(
                    items = json.decodeFromString<List<DownloadCredentialsFromIntegration>>(jsonData),
                    options = AddDownloadOptionsFromIntegration(
                        silentAdd = false,
                        silentStart = false,
                    )
                )
            }
        }
    }
}
