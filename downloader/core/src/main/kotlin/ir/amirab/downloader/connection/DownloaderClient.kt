package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials

abstract class DownloaderClient {
    fun defaultHeaders() = linkedMapOf(
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

