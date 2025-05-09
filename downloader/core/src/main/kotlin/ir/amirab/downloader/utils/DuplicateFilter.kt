package ir.amirab.downloader.utils

import ir.amirab.downloader.downloaditem.DownloadItem
import java.io.File

interface DuplicateDownloadFilter {
    fun isDuplicate(downloadItem: DownloadItem): Boolean
}

// I moved this logic here because it used multiple times
class DuplicateFilterByPath(
    private val file: File,
) : DuplicateDownloadFilter {
    override fun isDuplicate(downloadItem: DownloadItem): Boolean {
        return file == File(downloadItem.folder, downloadItem.name)
    }
}
