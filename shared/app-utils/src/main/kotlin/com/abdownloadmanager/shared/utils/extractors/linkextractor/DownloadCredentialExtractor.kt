package com.abdownloadmanager.shared.utils.extractors.linkextractor

import com.abdownloadmanager.shared.utils.extractors.Extractor
import ir.amirab.downloader.downloaditem.DownloadCredentials


interface DownloadCredentialExtractor<T>: Extractor<T, List<DownloadCredentials>> {
    override fun extract(input: T): List<DownloadCredentials>
}


object DownloadCredentialFromStringExtractor : DownloadCredentialExtractor<String> {
    override fun extract(input: String): List<DownloadCredentials> {
        return StringUrlExtractor.extract(input)
            .map { DownloadCredentials(link = it) }
    }

    fun parseCurlCommands(input: String): List<DownloadCredentials> {
        val curlCommands = input.split("\n").filter { it.trim().startsWith("curl") }
        return curlCommands.map { command ->
            val urlRegex = """curl\s+"([^"]+)"""".toRegex()
            val headerRegex = """-H\s+"([^"]+)"""".toRegex()
            
            val urlMatch = urlRegex.find(command)
            val headerMatches = headerRegex.findAll(command)
            
            val url = urlMatch?.groupValues?.get(1) ?: ""
            val headers = headerMatches.mapNotNull { match ->
                val header = match.groupValues[1]
                val (key, value) = header.split(":", limit = 2)
                key.trim() to value.trim()
            }.toMap()
            
            DownloadCredentials(
                link = url,
                headers = headers
            )
        }
    }
}