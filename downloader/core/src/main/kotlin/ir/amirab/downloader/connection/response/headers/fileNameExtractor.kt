package ir.amirab.downloader.connection.response.headers

import java.net.URLDecoder

fun extractFileNameFromContentDisposition(contentDispositionValue: String): String? {
    val utf8Regex = """filename\*=UTF-8''(?<fileName>[\w%\-\.]+)(?:; ?|${'$'})""".toRegex()
    utf8Regex.find(contentDispositionValue)
        ?.groups?.get("fileName")
        ?.value?.let {
            runCatching { URLDecoder.decode(it, Charsets.UTF_8) }
                .getOrNull()
        }?.let {
            return it
        }
    val asciiRegex = """filename=(["']?)(?<fileName>.*?[^\\])\1(?:; ?|$)""".toRegex()
    asciiRegex.find(contentDispositionValue)
        ?.groups
        ?.get("fileName")
        ?.value?.let {
            runCatching { URLDecoder.decode(it, Charsets.UTF_8) }
                .getOrNull()
        }?.let {
            return it
        }
    return null
}
