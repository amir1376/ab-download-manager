package com.abdownloadmanager.shared.util.ondownloadcompletion

import com.xeton.downloader.downloaditem.IDownloadItem

interface OnDownloadCompletionAction {
    suspend fun onDownloadCompleted(downloadItem: IDownloadItem)
}
