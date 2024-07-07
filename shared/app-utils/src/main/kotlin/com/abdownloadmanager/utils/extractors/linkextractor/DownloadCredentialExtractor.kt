package com.abdownloadmanager.utils.extractors.linkextractor

import ir.amirab.downloader.downloaditem.DownloadCredentials
import com.abdownloadmanager.utils.extractors.Extractor


interface DownloadCredentialExtractor<T>: Extractor<T, List<DownloadCredentials>> {
    override fun extract(input: T): List<DownloadCredentials>
}


object DownloadCredentialFromStringExtractor : DownloadCredentialExtractor<String> {
    override fun extract(input: String): List<DownloadCredentials> {
        return StringUrlExtractor.extract(input)
            .map { DownloadCredentials(link = it) }
    }
}