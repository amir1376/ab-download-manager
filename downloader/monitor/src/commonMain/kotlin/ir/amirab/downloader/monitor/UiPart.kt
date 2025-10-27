package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.part.MediaSegment
import ir.amirab.downloader.part.RangedPart
import ir.amirab.downloader.part.PartDownloadStatus

@Immutable
sealed interface UiPart {
    val id: Long
    val status: PartDownloadStatus
    val howMuchProceed: Long
    val percent: Int?
    val length: Long?
    val partSpace: Float
}

@Immutable
data class UiRangedPart(
    override val id: Long,
    override val status: PartDownloadStatus,
    override val howMuchProceed: Long,
    override val percent: Int?,
    override val length: Long?,
    override val partSpace: Float
) : UiPart {
    companion object {
        fun fromPart(
            part: RangedPart,
            totalLength: Long,
        ): UiRangedPart {
            val length = part.partLength
            return UiRangedPart(
                id = part.getID(),
                status = part.status,
                howMuchProceed = part.howMuchProceed(),
                percent = part.percent,
                length = length,
                partSpace = when {
                    totalLength <= 0 || length == null || length <= 0L -> 0f
                    else -> (length.toDouble() / totalLength.toDouble()).toFloat()
                },
            )
        }
    }
}

@Immutable
data class UiDurationBasedPart(
    override val id: Long,
    override val status: PartDownloadStatus,
    override val howMuchProceed: Long,
    override val percent: Int?,
    override val length: Long?,
    override val partSpace: Float,
//    val duration: Double,
) : UiPart {
    companion object {
        fun fromPart(
            part: MediaSegment,
            totalPartsCount: Int,
        ): UiDurationBasedPart {
            val index = part.segmentIndex
            return UiDurationBasedPart(
                id = index,
                status = part.status,
                howMuchProceed = part.howMuchProceed(),
                percent = part.percent,
                length = part.length,
                partSpace = if (totalPartsCount == 0) {
                    0f
                } else {
                    1f / totalPartsCount
                }
//                duration = part.duration,
            )
        }
    }
}
