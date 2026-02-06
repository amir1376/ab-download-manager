package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.IHttpBasedDownloadCredentials
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.utils.throwIf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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


    protected abstract suspend fun actualHead(
        credentials: IHttpDownloadCredentials,
        start: Long?,
        end: Long?,
    ): HttpResponseInfo

    protected abstract suspend fun actualConnect(
        credentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Connection<HttpResponseInfo>

    suspend fun head(
        credentials: IHttpDownloadCredentials,
        start: Long?,
        end: Long?,
    ): HttpResponseInfo {
        return usingNetwork {
            actualHead(credentials, start, end)
        }
    }

    suspend fun connect(
        credentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Connection<HttpResponseInfo> {
        return usingNetwork {
            actualConnect(credentials, start, end)
        }
    }

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

    private suspend fun <T> usingNetwork(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    companion object {
        fun createRangeHeader(start: Long, end: Long?) = "Range" to "bytes=$start-${end ?: ""}"
        fun getDefaultUserAgent(): String = UserAgent.getDefault()
    }
}

