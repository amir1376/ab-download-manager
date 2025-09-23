package ir.amirab.downloader.part

import kotlinx.coroutines.flow.MutableStateFlow

interface DownloadPart {
    var current: Long

    // internal usage do not change it!
    val statusFlow: MutableStateFlow<PartDownloadStatus>
    val status get() = statusFlow.value
    val isCompleted: Boolean
}
