package ir.amirab.downloader.part

import kotlinx.coroutines.flow.MutableStateFlow

interface DownloadPart {
    var current: Long

    // internal usage do not change it!
    val statusFlow: MutableStateFlow<PartDownloadStatus>
    val status get() = statusFlow.value
    val isCompleted: Boolean
    val percent: Int?
    fun howMuchProceed(): Long
    fun resetCurrent()

    // an id which also is sortable among the other parts
    // 1, 2, 3, ...
    fun getID(): Long
}
