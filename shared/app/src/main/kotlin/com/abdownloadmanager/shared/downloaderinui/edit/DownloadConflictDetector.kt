package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.downloader.downloaditem.IDownloadItem

interface IDownloadConflictDetector<in TDownloadItem : IDownloadItem> {
    fun checkAlreadyExists(
        current: TDownloadItem,
        edited: TDownloadItem,
    ): Boolean
}

class DownloadConflictDetector(
    private val downloadSystem: DownloadSystem
) : IDownloadConflictDetector<IDownloadItem> {
    override fun checkAlreadyExists(current: IDownloadItem, edited: IDownloadItem): Boolean {
        val currentDownloadFile = downloadSystem.getDownloadFile(current)
        val editedDownloadFile = downloadSystem.getDownloadFile(edited)
        val alreadyExists = editedDownloadFile.exists()
        if (alreadyExists) {
            return !(
                currentDownloadFile.parentFile?.canonicalPath == editedDownloadFile.parentFile?.canonicalPath &&
                currentDownloadFile.name.equals(editedDownloadFile.name, true)
            )
        }
        return downloadSystem
            .getAllRegisteredDownloadFiles()
            .contains(editedDownloadFile)
    }
}
