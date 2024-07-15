package ir.amirab.util

import java.net.URL
import java.net.URLDecoder

object UrlUtils {
    fun extractNameFromLink(link: String): String? {
        return runCatching {
            URL(link)
        }.map { url ->
            val foundName = url.path
                .split("/")
                .filter { it.isNotBlank()}
                .lastOrNull()?.let {
                    kotlin.runCatching {
                        URLDecoder.decode(it,Charsets.UTF_8)
                    }.getOrNull()
                }
            if (foundName != null) {
                return@map foundName
            }
            url.host.replace('.', '_')
        }
            .getOrNull()
    }
}
