package ir.amirab.downloader.connection

import okio.Source
import java.io.Closeable

data class Connection<out TResponseInfo : IResponseInfo>(
    val source: Source,
    val contentLength: Long,
    val responseInfo: TResponseInfo,
) : Closeable {
    override fun close() {
        source.close()
    }
}
