package ir.amirab.downloader.downloaditem.http

import ir.amirab.util.HttpUrlUtils
import kotlinx.serialization.Serializable

@Serializable
data class HttpDownloadBundleSource(
    val link: String,
    val headers: Map<String, String>? = null,
    val suggestedName: String,
    val contentLength: Long,
) {
    fun validate() {
        require(HttpUrlUtils.isValidUrl(link)) { "bundle source URL is not valid" }
        require(suggestedName.isNotBlank()) { "bundle source name is blank" }
        require(contentLength >= 0) {
            "bundle source length is invalid"
        }
    }

    fun asCredentials(downloadPage: String?): HttpDownloadCredentials {
        return HttpDownloadCredentials(
            link = link,
            headers = headers,
            downloadPage = downloadPage,
        )
    }
}

@Serializable
data class HttpDownloadBundle(
    val sources: List<HttpDownloadBundleSource>,
    val suggestedName: String,
) {
    val totalContentLength: Long
        get() {
            return sources.fold(0L) { total, source ->
                Math.addExact(total, source.contentLength)
            }
        }

    fun validate() {
        require(sources.size > 1) { "an HTTP bundle must contain at least two sources" }
        require(suggestedName.isNotBlank()) { "bundle name is blank" }
        sources.forEach(HttpDownloadBundleSource::validate)
        totalContentLength
    }
}
