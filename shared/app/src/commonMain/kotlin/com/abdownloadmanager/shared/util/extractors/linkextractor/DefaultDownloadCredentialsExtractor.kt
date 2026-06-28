package com.abdownloadmanager.shared.util.extractors.linkextractor

import ir.amirab.downloader.downloaditem.IDownloadCredentials


object DefaultDownloadCredentialsExtractor :
    DownloadCredentialExtractor<String> {
    override fun extract(input: String): List<IDownloadCredentials> {
        val stringExtractors = listOf(
            DownloadCredentialsFromJson,
            DownloadCredentialsFromCurl,
            DownloadCredentialsFromSimpleLink,
        )
        val items = stringExtractors.firstNotNullOfOrNull { extractor ->
            runCatching {
                extractor
                    .extract(input)
                    .takeIf { it.isNotEmpty() }?.also {
                        println("$extractor used")
                    }
            }.getOrElse { null }
        }?.distinctBy { it.link } ?: emptyList()
        return items
    }
}
