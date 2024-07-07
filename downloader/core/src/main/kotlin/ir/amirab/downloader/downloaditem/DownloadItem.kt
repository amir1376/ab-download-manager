package ir.amirab.downloader.downloaditem

import kotlinx.serialization.Serializable

@Serializable
data class DownloadItem(
    override var link: String,
    override var headers: Map<String, String>? = null,
    override var username: String? = null,
    override var password: String? = null,
    override var downloadPage: String? = null,
    override var userAgent: String? = null,

    var id: Long,
    var folder: String,
    var name: String,

    var contentLength: Long = LENGTH_UNKNOWN,
    var serverETag:String? = null,

    var dateAdded: Long = 0,
    var startTime: Long? = null,
    var completeTime: Long? = null,
    var status: DownloadStatus = DownloadStatus.Added,
    var preferredConnectionCount: Int? = null,
    var speedLimit: Long = 0,//0 is unlimited
) : IDownloadCredentials {
    companion object {
        const val LENGTH_UNKNOWN = -1L
    }
}

fun DownloadItem.withCredentials(credentials: IDownloadCredentials) = apply {
    link = credentials.link
    headers = credentials.headers
    username = credentials.username
    password = credentials.password
    downloadPage = credentials.downloadPage
    userAgent = credentials.userAgent
}

enum class DownloadStatus {
    Error,
    Added,
    Paused,
    Downloading,
    Completed,
}
