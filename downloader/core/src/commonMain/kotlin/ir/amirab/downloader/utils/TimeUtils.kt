package ir.amirab.downloader.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun convertLastModifiedHeaderToTimestamp(lastModified: String): Long {
        // Define the format of the Last-Modified header
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)

        // Parse the date string into a Date object
        val date = dateFormat.parse(lastModified)

        // Convert the Date object to a timestamp (milliseconds since epoch)
        return date?.time ?: throw IllegalArgumentException("Invalid Last-Modified header")
    }
}