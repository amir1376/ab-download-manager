package ir.amirab.util

import ir.amirab.util.platform.Platform
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.net.URLDecoder

object UrlUtils {
    fun extractNameFromLink(link: String): String? {
        return runCatching {
            URL(link)
        }.map { url ->
            val foundName = url.path
                .split("/")
                .filter { it.isNotBlank() }
                .lastOrNull()?.let {
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

    fun openUrl(url: String) {
        kotlin.runCatching {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        }
    }
}
