package com.abdownloadmanager.shared.utils.ondownloadcompletion

import ir.amirab.downloader.downloaditem.IDownloadItem

interface OnDownloadCompletionAction {
    suspend fun onDownloadCompleted(downloadItem: IDownloadItem)
}
