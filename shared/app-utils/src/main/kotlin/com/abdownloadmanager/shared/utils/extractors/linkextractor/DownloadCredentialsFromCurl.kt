package com.abdownloadmanager.shared.utils.extractors.linkextractor

import ir.amirab.downloader.downloaditem.DownloadCredentials
import com.abdownloadmanager.shared.utils.extractors.Extractor

object DownloadCredentialsFromCurl : DownloadCredentialExtractor<String> {
    override fun extract(input: String): List<DownloadCredentials> {
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
            val usernamePasswordRegex = """(?:-u|--user)\s+([^:]+):(.*)""".toRegex()
            val usernamePasswordMatch = usernamePasswordRegex.find(command)
            val username = usernamePasswordMatch?.groupValues?.get(1)?.trim()
            val password = usernamePasswordMatch?.groupValues?.get(2)?.trim()
            DownloadCredentials(
                link = url,
                headers = headers,
                username = username,
                password = password
            )
        }
    }

    fun generateCurlCommands(credentialsList: List<DownloadCredentials>): List<String> {
        return credentialsList.map { credentials ->
            val curlCommand = StringBuilder("curl \"${credentials.link}\"")
            credentials.headers?.forEach { (headerName, headerValue) ->
                curlCommand.append(" -H \"${headerName}: ${headerValue}\"")
            }
            if (credentials.username != null) {
                if (credentials.password != null) {
                    curlCommand.append(" -u \"${credentials.username}:${credentials.password}\"")
                } else {
                    curlCommand.append(" -u \"${credentials.username}\"")
                }
            }
            curlCommand.toString()
        }
    }
}
