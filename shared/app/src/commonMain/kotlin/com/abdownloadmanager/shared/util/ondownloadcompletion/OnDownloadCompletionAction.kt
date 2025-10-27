package com.abdownloadmanager.shared.util.ondownloadcompletion

import ir.amirab.downloader.downloaditem.IDownloadItem

interface OnDownloadCompletionAction {
    suspend fun onDownloadCompleted(downloadItem: IDownloadItem)
}
