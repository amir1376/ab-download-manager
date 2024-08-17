package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials

abstract class DownloaderClient {
    /**
     * these headers will be placed at first and maybe overridden by another header
     */
    fun defaultHeadersInFirst() = linkedMapOf<String,String>(
        //empty for now!
    )

    /**
     * these headers will be added after others so they override existing headers
     */
    fun defaultHeadersInLast() = linkedMapOf(
        "accept-encoding" to "identity",
    )


    abstract suspend fun head(credentials: IDownloadCredentials): ResponseInfo
    abstract suspend fun connect(
        credentials: IDownloadCredentials,
        start: Long,
        end: Long?,
    ): Connection

    companion object {
        fun createRangeHeader(start: Long, end: Long?) = "Range" to "bytes=$start-${end ?: ""}"
    }
}

