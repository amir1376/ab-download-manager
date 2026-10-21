package ir.amirab.downloader.connection

import okhttp3.HttpUrl

/** Only the host's share links need resolution; direct /dl/ URLs pass through. */
internal object FuckingFastPage {
    private val sharePath = Regex("""/(?:f/)?([a-zA-Z0-9]{12})/?""")
    private val quotedDownloadUrl = Regex(
        """["']((?:(?:https?:)?//(?:dl\.)?fuckingfast\.co)?/dl/[^\s"'<>\\]+)["']""",
        RegexOption.IGNORE_CASE,
    )

    fun isLandingPage(url: HttpUrl): Boolean {
        return url.host in setOf("fuckingfast.co", "www.fuckingfast.co") &&
            sharePath.matches(url.encodedPath)
    }

    fun downloadButtonUrl(pageUrl: HttpUrl): HttpUrl {
        val id = requireNotNull(sharePath.matchEntire(pageUrl.encodedPath)).groupValues[1]
        return pageUrl.newBuilder()
            .encodedPath("/f/$id/go")
            .query(null)
            .fragment(null)
            .build()
    }

    fun parseDownloadUrl(target: String, pageUrl: HttpUrl): HttpUrl? {
        val decoded = target.trim()
            .replace("\\/", "/")
            .replace("\\u002f", "/", ignoreCase = true)
            .replace("\\u0026", "&", ignoreCase = true)
            .replace("&amp;", "&")
            .replace("&#38;", "&")
            .replace("&#x26;", "&", ignoreCase = true)
        val url = pageUrl.resolve(decoded) ?: return null
        return url.takeIf {
            it.host in setOf("fuckingfast.co", "www.fuckingfast.co", "dl.fuckingfast.co") &&
                it.encodedPath.startsWith("/dl/") && it.encodedPath.length > 4 &&
                it.username.isEmpty() && it.password.isEmpty() &&
                (!pageUrl.isHttps || it.isHttps)
        }
    }

    fun extractDownloadUrl(html: String, pageUrl: HttpUrl): HttpUrl? {
        val normalized = html.replace("\\/", "/")
            .replace("\\u002f", "/", ignoreCase = true)
        return quotedDownloadUrl.findAll(normalized)
            .mapNotNull { parseDownloadUrl(it.groupValues[1], pageUrl) }
            .firstOrNull()
    }
}
