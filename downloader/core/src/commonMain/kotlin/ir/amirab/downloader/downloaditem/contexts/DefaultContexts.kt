package ir.amirab.downloader.downloaditem.contexts

import ir.amirab.downloader.downloaditem.DownloadItemContext

interface CanPerformRemove
interface CanPerformResume
interface CanPerformPause

object User : CanPerformPause, CanPerformResume, CanPerformRemove
object DuplicateRemoval : CanPerformRemove
data class Queue(val queue: Long) : CanPerformPause, CanPerformResume, CanPerformRemove

data class StoppedBy(
    val by: CanPerformPause
) : DownloadItemContext.Element {
    companion object Key : DownloadItemContext.Key<StoppedBy>

    override fun getKey(): DownloadItemContext.Key<*> = Key
}

data class ResumedBy(
    val by: CanPerformPause
) : DownloadItemContext.Element {
    companion object Key : DownloadItemContext.Key<ResumedBy>

    override fun getKey(): DownloadItemContext.Key<*> = Key
}

data class RemovedBy(
    val by: CanPerformRemove
) : DownloadItemContext.Element {
    companion object Key : DownloadItemContext.Key<RemovedBy>

    override fun getKey(): DownloadItemContext.Key<*> = Key
}

/**
 * information that is sent supplied to each download context by the [ir.amirab.downloader.DownloadManager]
 */
data class ResumingInfo(
    val isFirstAttempt: Boolean,
) : DownloadItemContext.Element {
    companion object Key : DownloadItemContext.Key<ResumingInfo>

    override fun getKey(): DownloadItemContext.Key<*> = Key
}
