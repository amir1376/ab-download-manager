package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.utils.calcPercent

@Immutable
data class ProcessingDownloadItemState(
    override val id: Long,
    override val folder: String,
    override val name: String,
    override val downloadLink: String,
    override val contentLength: Long,
    override val saveLocation: String,
    override val dateAdded: Long,
    override val startTime: Long,
    override val completeTime: Long,
    val status: DownloadJobStatus,
    val speed: Long,
    val parts: List<UiPart>,
    val supportResume: Boolean?,
) : IDownloadItemState {
    val progress = parts.sumOf {
        it.howMuchProceed
    }
    val hasProgress get() = progress > 0
    val gotAnyProgress= progress > 0L
    val percent: Int? = if (contentLength == DownloadItem.LENGTH_UNKNOWN) {
        null
    } else {
        calcPercent(progress, contentLength)
    }

    //remaining time in seconds
    val remainingTime: Long? = kotlin.run {
        when {
            contentLength <= 0 || speed <= 0 -> null
            else -> (contentLength - progress) / speed
        }
    }

    companion object {
        const val SPEED_PER_UNIT = "s"
        fun fromDownloadJob(
            downloadJob: DownloadJob,
            speed: Long,
        ): ProcessingDownloadItemState {
            val downloadItem = downloadJob.downloadItem
            val downloadJobStatus = downloadJob.status.value
            val parts = downloadJob.getParts()
            return ProcessingDownloadItemState(
                id = downloadItem.id,
                folder = downloadItem.folder,
                name = downloadItem.name,
                contentLength = downloadItem.contentLength ?: -1,
                dateAdded = downloadItem.dateAdded,
                startTime = downloadItem.startTime ?: -1,
                completeTime = downloadItem.completeTime ?: -1,
                status = downloadJobStatus,
                saveLocation = downloadItem.name,
                parts = parts.map {
                    UiPart.fromPart(it)
                },
                speed = speed,
                supportResume = downloadJob.supportsConcurrent,
                downloadLink = downloadItem.link
            )
        }

        fun onlyDownloadItem(
            downloadItem: DownloadItem,
        ): ProcessingDownloadItemState {
            val downloadJobStatus = DownloadJobStatus.IDLE
            return ProcessingDownloadItemState(
                id = downloadItem.id,
                folder = downloadItem.folder,
                name = downloadItem.name,
                contentLength = downloadItem.contentLength ?: -1,
                dateAdded = downloadItem.dateAdded,
                startTime = downloadItem.startTime ?: -1,
                completeTime = downloadItem.completeTime ?: -1,
                status = downloadJobStatus,
                saveLocation = downloadItem.name,
                parts = emptyList(),
                speed = 0,
                downloadLink = downloadItem.link,
                supportResume = null,
            )
        }
    }
}