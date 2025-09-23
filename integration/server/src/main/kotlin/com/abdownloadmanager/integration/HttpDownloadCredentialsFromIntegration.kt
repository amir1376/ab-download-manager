package com.abdownloadmanager.integration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface IDownloadCredentialsFromIntegration {
    val link: String
    val downloadPage: String?
}

@SerialName("http")
@Serializable
data class HttpDownloadCredentialsFromIntegration(
    override val link: String,
    val headers: Map<String, String>? = null,
    override val downloadPage: String? = null,
) : IDownloadCredentialsFromIntegration

@Serializable
data class AddDownloadOptionsFromIntegration(
    val silentAdd: Boolean = false,
    val silentStart: Boolean = false,
)

@Serializable
data class AddDownloadsFromIntegration(
    val items: List<IDownloadCredentialsFromIntegration>,
    val options: AddDownloadOptionsFromIntegration = AddDownloadOptionsFromIntegration()
) {
    companion object {
        fun createFromRequest(json: Json, jsonData: String): AddDownloadsFromIntegration {
            return json.decodeFromString<AddDownloadsFromIntegration>(jsonData)
        }
    }
}
