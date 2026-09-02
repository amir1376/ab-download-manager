package ir.amirab.util

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object HttpUrlUtils {
    fun createURL(url: String): HttpUrl {
        return url.toHttpUrl()
    }

    fun isValidUrl(link: String): Boolean {
        return runCatching { createURL(link) }.isSuccess
    }

    fun extractNameFromLink(link: String): String? {
        fun extractNameFromQuery(url: HttpUrl): String? {
            val fileNameQuery = url.queryParameterNames.firstOrNull {
                it.equals("filename", ignoreCase = true)
            } ?: return null
            return url.queryParameter(fileNameQuery)
                ?.takeIf { it.isNotBlank() }
        }

        fun extractNameFromLastPath(url: HttpUrl): String? {
            return url.pathSegments
                .lastOrNull { it.isNotBlank() }
                ?.let {
                    runCatching {
                        FilenameDecoder.decode(it, Charsets.UTF_8)
                    }.getOrNull()
                }
        }

        return runCatching {
            createURL(link)
        }.map { url ->
            extractNameFromQuery(url)
                ?: extractNameFromLastPath(url)
                ?: url.host.replace('.', '_')
        }.getOrNull()
    }

    fun getHost(url: String): String? {
        return kotlin.runCatching {
            createURL(url).host
        }.getOrNull()
    }

}
