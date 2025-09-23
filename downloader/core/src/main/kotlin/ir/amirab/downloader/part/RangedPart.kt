package ir.amirab.downloader.part

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class RangedPart(
    var from: Long,
    @Volatile
    var to: Long?,
    @Volatile
    override var current: Long = from,
) : DownloadPart {

    fun howMuchProceed(): Long {
        return current - from
    }

    fun resetCurrent() {
        current = from
    }

    @Transient
    override val statusFlow = MutableStateFlow<PartDownloadStatus>(PartDownloadStatus.IDLE)

    val remainingLength
        get() = to?.let {
            (it - current) + 1
        }
    val partLength
        get() = to?.let {
            (it - from) + 1
        }

    val percent
        get() = run {
            val partLength = partLength ?: return@run null
            (howMuchProceed().toDouble() / partLength.toDouble()) * 100
        }?.toInt()

    //    because of end inclusive completed position will be $to + 1
    override val isCompleted: Boolean
        get() = to?.let {
            current == it + 1
        } ?: false

    fun setBlindAsCompleted() {
        to = current - 1
    }

    val range
        get() = to?.let {
            from..it
        }

    val isBlind get() = to == null

    companion object {
    }
}
