package com.abdownloadmanager.shared.utils.ondownloadcompletion

import ir.amirab.downloader.downloaditem.DownloadItem

interface OnDownloadCompletionActionProvider {
    suspend fun getOnDownloadCompletionAction(downloadItem: DownloadItem): List<OnDownloadCompletionAction>
}

class NoOpOnDownloadCompletionActionProvider : OnDownloadCompletionActionProvider {
    override suspend fun getOnDownloadCompletionAction(downloadItem: DownloadItem): List<OnDownloadCompletionAction> {
        return emptyList()
    }
}
