package ir.amirab.downloader.part

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * the base interface for any downloader that may have parts
 * it is saved into storage for future resuming support
 */
@Serializable
sealed interface Parts {
    fun clone(): Parts
}

/**
 * this type is used for downloaders that split download into multiple byte ranges,
 * like http
 */
@SerialName("ranged")
@Serializable
data class RangedParts(
    val list: List<RangedPart>
) : Parts {
    override fun clone(): Parts {
        return copy()
    }
}
