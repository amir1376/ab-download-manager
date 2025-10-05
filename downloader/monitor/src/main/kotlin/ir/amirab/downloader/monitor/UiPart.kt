package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import ir.amirab.downloader.part.RangedPart
import ir.amirab.downloader.part.PartDownloadStatus

@Immutable
data class UiPart(
    val from: Long,
    val to: Long?,
    val current: Long,
    val status: PartDownloadStatus,
    val howMuchProceed: Long,
    val percent: Int?,
    val length: Long?,
) {
    companion object {
        fun fromPart(part: RangedPart): UiPart {
            return UiPart(
                from = part.from,
                to = part.to,
                current = part.current,
                status = part.status,
                howMuchProceed = part.howMuchProceed(),
                percent = part.percent,
                length = part.partLength,
            )
        }
    }
}
