package com.abdownloadmanager.shared.util.extractors.linkextractor

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.util.logger.appLogger
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DownloadCredentialsFromJson : DownloadCredentialExtractor<String>, KoinComponent {
    private val json: Json by inject()
    private val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    private val jsonWithoutEncoded: Json by lazy {
        Json(json) {
            encodeDefaults = false
        }
    }

    override fun extract(input: String): List<IDownloadCredentials> {
        return runCatching {
            listOf(json.decodeFromString<IDownloadCredentials>(input))
        }.recover {
            json.decodeFromString<List<IDownloadCredentials>>(input)
        }.getOrElse { emptyList() }
    }

    fun credentialsAsJson(items: List<IDownloadItem>): String {
        val json = jsonWithoutEncoded
        return runCatching {
            val credentials = items.map {
                val downloaderInUi = requireNotNull(
                    downloaderInUiRegistry.getDownloaderOf(it)
                )
                downloaderInUi.downloader.convertToCredentials(it)
            }
            return if (credentials.size == 1) {
                json.encodeToString(credentials.first())
            } else {
                json.encodeToString(credentials)
            }
        }.onFailure {
            appLogger.e("fail to export download credentials", it)
        }.getOrElse { "[]" }
    }
}
