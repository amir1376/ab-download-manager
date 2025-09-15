package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import okio.Source
import java.io.Closeable

data class Connection(
    val source: Source,
    val contentLength: Long,
    val closeable: Closeable,
    val responseInfo: ResponseInfo,
)
