package ir.amirab.downloader.downloaditem.http

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.IDownloadItem.Companion.LENGTH_UNKNOWN
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("http")
data class HttpDownloadItem(
    override var link: String,
    override var headers: Map<String, String>? = null,
    override var username: String? = null,
    override var password: String? = null,
    override var downloadPage: String? = null,
    override var userAgent: String? = null,

    var serverETag: String? = null,

//    IDownloadItem
    override var id: Long,
    override var folder: String,
    override var name: String,
    override var contentLength: Long = LENGTH_UNKNOWN,
    override var dateAdded: Long = 0,
    override var startTime: Long? = null,
    override var completeTime: Long? = null,
    override var status: DownloadStatus = DownloadStatus.Added,
    override var preferredConnectionCount: Int? = null,
    override var speedLimit: Long = 0,//0 is unlimited
    override var fileChecksum: String? = null,
) : IDownloadItem, IHttpDownloadCredentials {
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
    ): HttpDownloadItem {
        val id = id.getOrElse { this.id }
        val folder = folder.getOrElse { this.folder }
        val name = name.getOrElse { this.name }
        val link = link.getOrElse { this.link }
        val contentLength = contentLength.getOrElse { this.contentLength }
        val downloadPage = downloadPage.getOrElse { this.downloadPage }
        val dateAdded = dateAdded.getOrElse { this.dateAdded }
        val startTime = startTime.getOrElse { this.startTime }
        val completeTime = completeTime.getOrElse { this.completeTime }
        val status = status.getOrElse { this.status }
        val preferredConnectionCount = preferredConnectionCount.getOrElse { this.preferredConnectionCount }
        val speedLimit = speedLimit.getOrElse { this.speedLimit }
        val fileChecksum = fileChecksum.getOrElse { this.fileChecksum }
        return copy(
            id = id,
            folder = folder,
            name = name,
            link = link,
            contentLength = contentLength,
            downloadPage = downloadPage,
            dateAdded = dateAdded,
            startTime = startTime,
            completeTime = completeTime,
            status = status,
            preferredConnectionCount = preferredConnectionCount,
            speedLimit = speedLimit,
            fileChecksum = fileChecksum,
        )
    }

    override fun copy(
        link: Option<String>,
        downloadPage: Option<String?>
    ): HttpDownloadItem {
        val link = link.getOrElse { this.link }
        val downloadPage = downloadPage.getOrElse { this.downloadPage }
        return copy(
            link = link,
            downloadPage = downloadPage,
        )
    }

    override fun validateCredentials() {
        //make sure url is valid
        HttpDownloadCredentials.validate(this)
    }

    override fun validateItem() {
        validateCredentials()
    }

    override fun withCredentials(credentials: IDownloadCredentials): HttpDownloadItem {
        return if (credentials is HttpDownloadCredentials) {
            withHttpCredentials(credentials)
        } else {
            this
        }
    }
}


fun HttpDownloadItem.applyFrom(other: HttpDownloadItem) {
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
    serverETag = other.serverETag

    dateAdded = other.dateAdded
    startTime = other.startTime
    completeTime = other.completeTime
    status = other.status
    preferredConnectionCount = other.preferredConnectionCount
    speedLimit = other.speedLimit

    fileChecksum = other.fileChecksum
}

fun HttpDownloadItem.withHttpCredentials(credentials: IHttpDownloadCredentials) = apply {
    link = credentials.link
    headers = credentials.headers
    username = credentials.username
    password = credentials.password
    downloadPage = credentials.downloadPage
    userAgent = credentials.userAgent
}

