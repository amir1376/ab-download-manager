package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.part.DownloadPart
import ir.amirab.downloader.part.PartDownloadStatus
import ir.amirab.downloader.utils.calcPercent
import kotlinx.coroutines.flow.MutableStateFlow

data class HttpBundlePart(
    val index: Long,
    val source: HttpDownloadBundleSource,
    @Volatile override var current: Long = 0,
) : DownloadPart {
    val length: Long get() = source.contentLength

    override val statusFlow = MutableStateFlow<PartDownloadStatus>(PartDownloadStatus.IDLE)

    override val isCompleted: Boolean
        get() = current == length

    override val percent: Int
        get() = if (length == 0L) 100 else calcPercent(current, length)

    override fun howMuchProceed(): Long = current

    override fun resetCurrent() {
        current = 0
        statusFlow.value = PartDownloadStatus.IDLE
    }

    override fun getID(): Long = index
}
