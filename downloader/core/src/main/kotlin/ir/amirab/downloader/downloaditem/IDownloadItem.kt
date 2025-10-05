package ir.amirab.downloader.downloaditem

import arrow.core.None
import arrow.core.Option

interface IDownloadItem : IDownloadCredentials {
    var id: Long
    var folder: String
    var name: String
    override var link: String
    var contentLength: Long
    override var downloadPage: String?
    var dateAdded: Long
    var startTime: Long?
    var completeTime: Long?
    var status: DownloadStatus
    var preferredConnectionCount: Int?
    var speedLimit: Long
    var fileChecksum: String?

    fun copy(
        id: Option<Long> = None,
        folder: Option<String> = None,
        name: Option<String> = None,
        link: Option<String> = None,
        contentLength: Option<Long> = None,
        downloadPage: Option<String?> = None,
        dateAdded: Option<Long> = None,
        startTime: Option<Long?> = None,
        completeTime: Option<Long?> = None,
        status: Option<DownloadStatus> = None,
        preferredConnectionCount: Option<Int?> = None,
        speedLimit: Option<Long> = None,
        fileChecksum: Option<String?> = None,
    ): IDownloadItem

    fun validateItem()
    fun withCredentials(credentials: IDownloadCredentials): IDownloadItem

    companion object {
        const val LENGTH_UNKNOWN = -1L
    }
}
