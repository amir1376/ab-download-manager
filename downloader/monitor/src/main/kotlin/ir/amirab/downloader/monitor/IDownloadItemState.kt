package ir.amirab.downloader.monitor

import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
sealed interface IDownloadItemState {
    val id: Long
    val folder: String
    val name: String
    val contentLength: Long
    val saveLocation: String
    val dateAdded:Long
    val startTime: Long
    val completeTime: Long
    val downloadLink:String

    fun getFullPath() = File(folder, name)
}