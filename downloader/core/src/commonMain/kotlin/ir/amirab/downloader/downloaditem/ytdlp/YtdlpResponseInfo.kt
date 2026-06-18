package ir.amirab.downloader.downloaditem.ytdlp

import ir.amirab.downloader.connection.IResponseInfo

data class YtdlpResponseInfo(
    override val isSuccessFul: Boolean,
    val title: String?,
    val ext: String?,
    val size: Long,
    override val unsuccessFullException: Throwable? = null
) : IResponseInfo {
    override val requiresAuth: Boolean = false
    override val requireBasicAuth: Boolean = false
    override val resumeSupport: Boolean = true
    override val isWebPage: Boolean = false
}
