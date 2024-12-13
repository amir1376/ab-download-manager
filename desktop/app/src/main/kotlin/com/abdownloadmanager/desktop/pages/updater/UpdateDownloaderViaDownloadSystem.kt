package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.updateapplier.UpdateDownloader
import com.abdownloadmanager.updatechecker.UpdateInfo
import com.abdownloadmanager.utils.DownloadSystem
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.EmptyContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class UpdateDownloaderViaDownloadSystem(
    private val downloadSystem: DownloadSystem,
    private val saveFolder: String,
) : UpdateDownloader {
    override suspend fun downloadUpdate(updateInfo: UpdateInfo): File {
        val id = downloadSystem.getOrCreateDownloadByLink(
            DownloadItem(
                id = -1,
                link = updateInfo.link,
                folder = saveFolder,
                name = updateInfo.name,
            )
        )
        coroutineScope {
            val waiter = async {
                downloadSystem.downloadMonitor.waitForDownloadToFinishOrCancel(id)
            }
            downloadSystem.manualResume(id, EmptyContext)
            waiter.await()
        }
        // we recheck download info maybe some dude change the file name!
        val downloadedItem = downloadSystem.getDownloadItemById(id)
        requireNotNull(downloadedItem) {
            "Download is removed!"
        }
        return downloadSystem.getDownloadFile(downloadedItem)
    }

    override suspend fun removeUpdate(updateInfo: UpdateInfo) {
        val item = downloadSystem
            .getDownloadItemByLink(updateInfo.link)
            .maxByOrNull { it.dateAdded }
        item?.let {
            downloadSystem.removeDownload(item.id, true, EmptyContext)
        }
    }
}