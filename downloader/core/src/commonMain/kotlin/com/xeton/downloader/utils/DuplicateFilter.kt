package com.xeton.downloader.utils

import com.xeton.downloader.downloaditem.IDownloadItem
import java.io.File

interface DuplicateDownloadFilter {
    fun isDuplicate(downloadItem: IDownloadItem): Boolean
}

// I moved this logic here because it used multiple times
class DuplicateFilterByPath(
    private val file: File,
) : DuplicateDownloadFilter {
    override fun isDuplicate(downloadItem: IDownloadItem): Boolean {
        return file == File(downloadItem.folder, downloadItem.name)
    }
}
