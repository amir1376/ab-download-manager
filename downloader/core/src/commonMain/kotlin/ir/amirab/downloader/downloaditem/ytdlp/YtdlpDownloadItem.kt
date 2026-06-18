package ir.amirab.downloader.downloaditem.ytdlp

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ytdlp")
data class YtdlpDownloadItem(
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
    var headers: Map<String, String>? = null,
    var username: String? = null,
    var password: String? = null,
    var userAgent: String? = null,
) : IDownloadItem {
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
    ): YtdlpDownloadItem {
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
    ): YtdlpDownloadItem {
        return copy(
            link = link.getOrElse { this.link },
            downloadPage = downloadPage.getOrElse { this.downloadPage },
        )
    }

    override fun validateItem() {
        validateCredentials()
    }

    override fun withCredentials(credentials: IDownloadCredentials): YtdlpDownloadItem {
        return if (credentials is YtdlpDownloadCredentials) {
            copy(
                link = credentials.link,
                downloadPage = credentials.downloadPage
            )
        } else {
            this
        }
    }

    override fun validateCredentials() {
        require(link.isNotBlank())
    }

    companion object {
        fun createWithCredentials(
            id: Long,
            credentials: YtdlpDownloadCredentials,
            folder: String,
            name: String,
            contentLength: Long = -1,
            dateAdded: Long = 0,
            startTime: Long? = null,
            completeTime: Long? = null,
            status: DownloadStatus = DownloadStatus.Added,
            preferredConnectionCount: Int? = null,
            speedLimit: Long = 0,
            fileChecksum: String? = null,
        ): YtdlpDownloadItem {
            return YtdlpDownloadItem(
                id = id,
                folder = folder,
                name = name,
                link = credentials.link,
                contentLength = contentLength,
                downloadPage = credentials.downloadPage,
                dateAdded = dateAdded,
                startTime = startTime,
                completeTime = completeTime,
                status = status,
                preferredConnectionCount = preferredConnectionCount,
                speedLimit = speedLimit,
                fileChecksum = fileChecksum,
            )
        }
    }
}
