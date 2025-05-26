package ir.amirab.util

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.URLDecoder

object UrlUtils {
    fun createURL(url: String): HttpUrl {
        return url.toHttpUrl()
    }

    fun isValidUrl(link: String): Boolean {
        return runCatching { createURL(link) }.isSuccess
    }

    fun extractNameFromLink(link: String): String? {
        return runCatching {
            createURL(link)
        }.map { url ->
            val foundName = url.pathSegments
                .lastOrNull { it.isNotBlank() }
                ?.let {
                    kotlin.runCatching {
                        URLDecoder.decode(it, Charsets.UTF_8)
                    }.getOrNull()
                }
            if (foundName != null) {
                return@map foundName
            }
            url.host.replace('.', '_')
        }
            .getOrNull()
    }

    fun getHost(url: String): String? {
        return kotlin.runCatching {
            createURL(url).host
        }.getOrNull()
    }

}
