package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
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
    val percent: Int? = if (contentLength == IDownloadItem.LENGTH_UNKNOWN) {
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

    companion object
}
