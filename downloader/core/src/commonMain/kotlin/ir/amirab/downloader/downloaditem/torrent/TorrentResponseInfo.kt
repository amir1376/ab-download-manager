package ir.amirab.downloader.downloaditem.torrent

import ir.amirab.downloader.connection.IResponseInfo
import java.io.IOException

data class TorrentResponseInfo(
    val name: String?,
    val duration: Double? = null,
) : IResponseInfo {
    override val isSuccessFul: Boolean get() = true
    override val requiresAuth: Boolean get() = false
    override val requireBasicAuth: Boolean get() = false
    override val isWebPage: Boolean get() = false
    override val resumeSupport: Boolean get() = true
    override val unsuccessFullException: Throwable? get() = null
}

class BadTorrentResponseException(
    message: String, cause: Throwable? = null
) : IOException(message, cause)
