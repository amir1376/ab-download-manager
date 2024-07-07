package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import okio.BufferedSource
import java.io.Closeable

data class Connection(
    val source: BufferedSource,
    val contentLength: Long,
    val closeable: Closeable,
    val responseInfo: ResponseInfo,
)