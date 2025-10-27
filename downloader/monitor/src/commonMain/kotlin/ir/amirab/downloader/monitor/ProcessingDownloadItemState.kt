package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.part.PartDownloadStatus
import ir.amirab.downloader.utils.calcPercent

sealed interface ProcessingDownloadItemState : IDownloadItemState {
    val status: DownloadJobStatus
    val speed: Long
    val supportResume: Boolean?
    val parts: List<UiPart>

    val gotAnyProgress: Boolean
    val progress: Long
    val hasProgress: Boolean
    val percent: Int? // 0..100

    //remaining time in seconds
    val remainingTime: Long?
}

@Immutable
data class RangeBasedProcessingDownloadItemState(
    override val id: Long,
    override val folder: String,
    override val name: String,
    override val downloadLink: String,
    override val contentLength: Long,
    override val saveLocation: String,
    override val dateAdded: Long,
    override val startTime: Long,
    override val completeTime: Long,
    override val status: DownloadJobStatus,
    override val speed: Long,
    override val parts: List<UiPart>,
    override val supportResume: Boolean?,
) : ProcessingDownloadItemState {
    override val progress = parts.sumOf {
        it.howMuchProceed
    }
    override val hasProgress get() = progress > 0
    override val gotAnyProgress = progress > 0L
    override val percent: Int? = if (contentLength == IDownloadItem.LENGTH_UNKNOWN) {
        null
    } else {
        calcPercent(progress, contentLength)
    }

    //remaining time in seconds
    override val remainingTime: Long? = kotlin.run {
        when {
            contentLength <= 0 || speed <= 0 -> null
            else -> (contentLength - progress) / speed
        }
    }

    companion object
}

@Immutable
data class DurationBasedProcessingDownloadItemState(
    override val id: Long,
    override val folder: String,
    override val name: String,
    override val downloadLink: String,
    override val contentLength: Long,
    override val saveLocation: String,
    override val dateAdded: Long,
    override val startTime: Long,
    override val completeTime: Long,
    override val status: DownloadJobStatus,
    override val speed: Long,
    override val parts: List<UiPart>,
    override val supportResume: Boolean?,
    val optimisticLength: Long,
    val duration: Double?,
    override val progress: Long,
    override val percent: Int
) : ProcessingDownloadItemState {

    override val hasProgress get() = progress > 0
    override val gotAnyProgress = progress > 0L
//    override val percent: Int? = run {
//        val length = getLengthOrOptimistic(contentLength, optimisticLength)
//        if (length == IDownloadItem.LENGTH_UNKNOWN) {
//            val partsSize = parts.size
//            if (partsSize > 0) {
//                calcPercent(finishedPartsCount, partsSize)
//            } else {
//                null
//            }
//        } else {
//            calcPercent(progress, length)
//        }
//    }

    override val remainingTime: Long? = kotlin.run {
        val length = getLengthOrOptimistic(contentLength, optimisticLength)
        when {
            length <= 0 || speed <= 0 -> null
            else -> (length - progress) / speed
        }
    }

    companion object {
        private fun getLengthOrOptimistic(
            exactLength: Long, optimisticLength: Long
        ): Long {
            return when {
                exactLength > 0 -> exactLength
                optimisticLength > 0 -> optimisticLength
                else -> -1
            }
        }
    }
}
