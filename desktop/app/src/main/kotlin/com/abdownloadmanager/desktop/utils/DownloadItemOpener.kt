package com.abdownloadmanager.desktop.utils

import ir.amirab.downloader.downloaditem.DownloadItem

interface DownloadItemOpener {
    suspend fun openDownloadItem(id:Long)
    fun openDownloadItem(downloadItem: DownloadItem)

    suspend fun openDownloadItemFolder(id:Long)
    fun openDownloadItemFolder(downloadItem: DownloadItem)
}