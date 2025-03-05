package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials

abstract class DownloaderClient {
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
        credentials: IDownloadCredentials,
        start: Long?,
        end: Long?,
    ): ResponseInfo

    abstract suspend fun connect(
        credentials: IDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Connection

    suspend fun test(credentials: IDownloadCredentials): ResponseInfo {
        val rangeStart = 0L
        val rangeEnd = 255L
        val rangeLength = rangeEnd - rangeStart + 1 // 256
        val response = head(credentials, rangeStart, rangeEnd)

        if (response.isSuccessFul && response.totalLength != rangeLength) {
            return response
        }
        // server may return un-standard response we use headless (without resuming support)
        return head(credentials, null, null)
    }

    companion object {
        fun createRangeHeader(start: Long, end: Long?) = "Range" to "bytes=$start-${end ?: ""}"
    }
}

