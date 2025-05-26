package com.abdownloadmanager.shared.utils

import ir.amirab.downloader.downloaditem.DownloadItem

interface DownloadItemOpener {
    suspend fun openDownloadItem(id:Long)
    suspend fun openDownloadItem(downloadItem: DownloadItem)

    suspend fun openDownloadItemFolder(id:Long)
    suspend fun openDownloadItemFolder(downloadItem: DownloadItem)
}
