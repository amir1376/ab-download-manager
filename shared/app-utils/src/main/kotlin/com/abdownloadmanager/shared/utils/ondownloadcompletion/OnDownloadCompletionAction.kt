package com.abdownloadmanager.shared.utils.ondownloadcompletion

import ir.amirab.downloader.downloaditem.DownloadItem

interface OnDownloadCompletionAction {
    suspend fun onDownloadCompleted(downloadItem: DownloadItem)
}
