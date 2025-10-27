package ir.amirab.downloader.connection.response.headers

import ir.amirab.downloader.connection.response.HttpResponseInfo

data class ContentRangeValue(
    val range: LongRange?,
    val fullSize: Long?,
)

fun HttpResponseInfo.getContentRange(): ContentRangeValue? {
    val value = responseHeaders["content-range"] ?: return null
    val actualValue = runCatching {
        // some servers don't append "bytes " to the start of the value
        value.removePrefix("bytes ")
    }.getOrNull() ?: return null
    if (actualValue.isBlank()) {
        return null
    }

    val (rangeString, sizeString) = actualValue
        .split("/")
        .takeIf { it.size >= 2 } ?: return null

    val range = try {
        if (rangeString != "*") {
            rangeString.split("-").map {
                it.toLong()
            }.let {
                it[0]..it[1]
            }
        } else {
            null
        }
    } catch (e: Exception) {
        // NumberFormatException or IndexOutOfBoundException
        return null
    }

    val size: Long? = if (sizeString != "*") {
        // some servers not returning * nor integer value.
        sizeString.toLongOrNull() ?: return null
    } else null

    return ContentRangeValue(
        range = range,
        fullSize = size,
    )

}
