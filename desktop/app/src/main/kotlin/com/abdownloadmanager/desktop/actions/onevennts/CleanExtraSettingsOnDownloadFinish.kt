package com.abdownloadmanager.desktop.actions.onevennts

import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.util.ondownloadcompletion.OnDownloadCompletionAction
import ir.amirab.downloader.downloaditem.IDownloadItem

class CleanExtraSettingsOnDownloadFinish(
    private val storage: IExtraDownloadSettingsStorage<*>
) : OnDownloadCompletionAction {
    override suspend fun onDownloadCompleted(downloadItem: IDownloadItem) {
        storage.deleteExtraDownloadItemSettings(downloadItem.id)
    }
}
