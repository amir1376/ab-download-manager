package com.abdownloadmanager.shared.utils

import ir.amirab.downloader.downloaditem.IDownloadItem

interface DownloadItemOpener {
    suspend fun openDownloadItem(id:Long)
    suspend fun openDownloadItem(downloadItem: IDownloadItem)

    suspend fun openDownloadItemFolder(id:Long)
    suspend fun openDownloadItemFolder(downloadItem: IDownloadItem)
}
