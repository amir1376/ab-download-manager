package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.utils.throwIf

abstract class HttpDownloaderClient {
    /**
     * these headers will be placed at first and maybe overridden by another header
     */
    fun defaultHeadersInFirst() = linkedMapOf<String, String>(
        //empty for now!
    )

    /**
     * these headers will be added after others so they override existing headers
     */
    fun defaultHeadersInLast() = linkedMapOf(
        "accept-encoding" to "identity",
    )


    abstract suspend fun head(
        credentials: IHttpDownloadCredentials,
        start: Long?,
        end: Long?,
    ): HttpResponseInfo

    abstract suspend fun connect(
        credentials: IHttpDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Connection<HttpResponseInfo>

    suspend fun test(credentials: IHttpDownloadCredentials): HttpResponseInfo {
        try {
            val rangeStart = 0L
            val rangeEnd = 255L
            val rangeLength = rangeEnd - rangeStart + 1 // 256
            val response = head(credentials, rangeStart, rangeEnd)

            if (response.isSuccessFul && response.totalLength != rangeLength) {
                return response
            }
        } catch (e: Exception) {
            e.throwIf { ExceptionUtils.isNormalCancellation(e) }
            // some servers may reset the connection (ECONNRESET) if we ask for bytes=0-255
            // so we don't provide resume support for them
        }
        // server may return un-standard response we use headless (without resuming support)
        return head(credentials, null, null)
    }

    companion object {
        fun createRangeHeader(start: Long, end: Long?) = "Range" to "bytes=$start-${end ?: ""}"
    }
}

