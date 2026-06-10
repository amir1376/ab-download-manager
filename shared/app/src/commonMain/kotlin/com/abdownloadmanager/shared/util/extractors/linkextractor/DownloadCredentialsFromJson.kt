package com.abdownloadmanager.shared.util.extractors.linkextractor

import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.util.logger.appLogger
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DownloadCredentialsFromJson : DownloadCredentialExtractor<String>, KoinComponent {
    val json: Json by inject()
    override fun extract(input: String): List<IDownloadCredentials> {
        return runCatching {
            listOf(json.decodeFromString<IDownloadCredentials>(input))
        }.recover {
            json.decodeFromString<List<IDownloadCredentials>>(input)
        }.getOrElse { emptyList() }
    }

    fun asJson(credentialsList: List<HttpDownloadCredentials>): String {
        return runCatching {
            return if (credentialsList.size == 1) {
                json.encodeToString(credentialsList.first())
            } else {
                json.encodeToString(credentialsList)
            }
        }.onFailure {
            appLogger.e("fail to export download credentials", it)
        }.getOrElse { "[]" }
    }
}
