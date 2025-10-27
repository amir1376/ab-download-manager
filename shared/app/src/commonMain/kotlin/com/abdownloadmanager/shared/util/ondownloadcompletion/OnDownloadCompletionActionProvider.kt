package com.abdownloadmanager.shared.util.ondownloadcompletion

import ir.amirab.downloader.downloaditem.IDownloadItem

interface OnDownloadCompletionActionProvider {
    suspend fun getOnDownloadCompletionAction(downloadItem: IDownloadItem): List<OnDownloadCompletionAction>
}

class NoOpOnDownloadCompletionActionProvider : OnDownloadCompletionActionProvider {
    override suspend fun getOnDownloadCompletionAction(downloadItem: IDownloadItem): List<OnDownloadCompletionAction> {
        return emptyList()
    }
}
