package com.abdownloadmanager.shared.pagemanager

import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.xeton.downloader.downloaditem.IDownloadItem

interface DownloadErrorDialogManager {
    fun openDownloadErrorDialog(downloadItem: IDownloadItem, reason: DownloadErrorReason)
    fun closeDownloadErrorDialog()
}
