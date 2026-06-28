package com.abdownloadmanager.shared.util

import com.xeton.downloader.downloaditem.IDownloadItem

interface DownloadItemOpener {
    suspend fun openDownloadItem(id:Long)
    suspend fun openDownloadItem(downloadItem: IDownloadItem)

    suspend fun openDownloadItemFolder(id:Long)
    suspend fun openDownloadItemFolder(downloadItem: IDownloadItem)
}
