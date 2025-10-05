package ir.amirab.downloader.downloaditem.hls

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.IHttpBasedDownloadCredentials
import ir.amirab.util.HttpUrlUtils
import kotlinx.serialization.Serializable
@Serializable
data class HLSDownloadItem(
    override var id: Long,
    override var folder: String,
    override var name: String,
    override var link: String,
    override var contentLength: Long = -1,
    override var downloadPage: String? = null,
    override var dateAdded: Long,
    override var startTime: Long? = null,
    override var completeTime: Long? = null,
    override var status: DownloadStatus = DownloadStatus.Added,
    override var preferredConnectionCount: Int? = null,
    override var speedLimit: Long = 0,
    override var fileChecksum: String? = null,
    override var headers: Map<String, String>? = null,
    override var username: String? = null,
    override var password: String? = null,
    override var userAgent: String? = null,
    var duration: Double? = null,
) : IDownloadItem, IHLSCredentials {
    override fun copy(
        id: Option<Long>,
        folder: Option<String>,
        name: Option<String>,
        link: Option<String>,
        contentLength: Option<Long>,
        downloadPage: Option<String?>,
        dateAdded: Option<Long>,
        startTime: Option<Long?>,
        completeTime: Option<Long?>,
        status: Option<DownloadStatus>,
        preferredConnectionCount: Option<Int?>,
        speedLimit: Option<Long>,
        fileChecksum: Option<String?>
    ): IDownloadItem {
        return copy(
            id = id.getOrElse { this.id },
            folder = folder.getOrElse { this.folder },
            name = name.getOrElse { this.name },
            link = link.getOrElse { this.link },
            contentLength = contentLength.getOrElse { this.contentLength },
            downloadPage = downloadPage.getOrElse { this.downloadPage },
            dateAdded = dateAdded.getOrElse { this.dateAdded },
            startTime = startTime.getOrElse { this.startTime },
            completeTime = completeTime.getOrElse { this.completeTime },
            status = status.getOrElse { this.status },
            preferredConnectionCount = preferredConnectionCount.getOrElse { this.preferredConnectionCount },
            speedLimit = speedLimit.getOrElse { this.speedLimit },
            fileChecksum = fileChecksum.getOrElse { this.fileChecksum },
        )
    }

    override fun copy(
        link: Option<String>,
        downloadPage: Option<String?>
    ): HLSDownloadItem {
        return copy(
            link = link.getOrElse { this.link },
            downloadPage = downloadPage.getOrElse { this.downloadPage },
        )
    }

    override fun validateItem() {
        validateCredentials()
    }

    override fun withCredentials(credentials: IDownloadCredentials): HLSDownloadItem {
        return if (credentials is IHLSCredentials) {
            withHlsCredentials(credentials)
        } else {
            this
        }
    }

    override fun validateCredentials() {
        HttpUrlUtils.isValidUrl(link)
    }

    companion object {
        fun createWithCredentials(
            credentials: IHLSCredentials,
            id: Long,
            folder: String,
            name: String,
            contentLength: Long = IDownloadItem.LENGTH_UNKNOWN,
            dateAdded: Long = 0,
            startTime: Long? = null,
            completeTime: Long? = null,
            status: DownloadStatus = DownloadStatus.Added,
            preferredConnectionCount: Int? = null,
            speedLimit: Long = 0,
            fileChecksum: String? = null,
            duration: Double? = null,
        ): HLSDownloadItem {
            return HLSDownloadItem(
                link = credentials.link,
                headers = credentials.headers,
                username = credentials.username,
                password = credentials.password,
                downloadPage = credentials.downloadPage,
                userAgent = credentials.userAgent,
                id = id,
                folder = folder,
                name = name,
                contentLength = contentLength,
                dateAdded = dateAdded,
                startTime = startTime,
                completeTime = completeTime,
                status = status,
                preferredConnectionCount = preferredConnectionCount,
                speedLimit = speedLimit,
                fileChecksum = fileChecksum,
                duration = duration,
            )
        }
    }
}

private fun HLSDownloadItem.withHlsCredentials(credentials: IHttpBasedDownloadCredentials) = apply {
    link = credentials.link
    headers = credentials.headers
    username = credentials.username
    password = credentials.password
    downloadPage = credentials.downloadPage
    userAgent = credentials.userAgent
}

fun HLSDownloadItem.applyFrom(other: HLSDownloadItem) {
    link = other.link
    headers = other.headers
    username = other.username
    password = other.password
    downloadPage = other.downloadPage
    userAgent = other.userAgent

    id = other.id
    folder = other.folder
    name = other.name

    contentLength = other.contentLength

    dateAdded = other.dateAdded
    startTime = other.startTime
    completeTime = other.completeTime
    status = other.status
    preferredConnectionCount = other.preferredConnectionCount
    speedLimit = other.speedLimit

    fileChecksum = other.fileChecksum
}
