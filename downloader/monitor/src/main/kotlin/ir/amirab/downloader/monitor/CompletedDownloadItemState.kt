package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.downloaditem.DownloadItem

@Immutable
data class CompletedDownloadItemState(
    override val id: Long,
    override val folder: String,
    override val name: String,
    override val downloadLink: String,
    override val contentLength: Long,
    override val saveLocation: String,
    override val dateAdded: Long,
    override val startTime: Long,
    override val completeTime: Long,
) : IDownloadItemState {
    companion object {
        fun fromDownloadItem(item: DownloadItem): CompletedDownloadItemState {
            return CompletedDownloadItemState(
                id = item.id,
                folder = item.folder,
                name = item.name,
                contentLength = item.contentLength,
                saveLocation = item.name,
                dateAdded = item.dateAdded,
                startTime = item.startTime ?: -1,
                completeTime = item.completeTime ?: -1,
                downloadLink = item.link,
            )
        }
    }
}