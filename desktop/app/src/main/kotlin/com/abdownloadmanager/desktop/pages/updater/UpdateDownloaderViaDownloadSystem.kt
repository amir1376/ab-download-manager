package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.UpdateDownloadLocationProvider
import com.abdownloadmanager.updateapplier.UpdateDownloader
import com.abdownloadmanager.updatechecker.UpdateSource
import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.utils.OnDuplicateStrategy
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class UpdateDownloaderViaDownloadSystem(
    private val downloadSystem: DownloadSystem,
    private val updateDownloadLocationProvider: UpdateDownloadLocationProvider,
) : UpdateDownloader {
    override suspend fun downloadUpdate(updateDirectDownloadLink: UpdateSource.DirectDownloadLink): File {
        val updateDownloadsFolder = updateDownloadLocationProvider.getSaveLocation().path
        val updateDownloads = downloadSystem.getDownloadItemsByFolder(updateDownloadsFolder)
        val pausedDownload = updateDownloads.find {
            it.name == updateDirectDownloadLink.name
        }
        // at the moment if the download was finished but removed from the filesystem
        // download will not be restarted automatically
        val requireRestartDownload = pausedDownload?.getFullPath()?.exists()?.not() ?: false
        val id = pausedDownload?.id
            ?: downloadSystem.addDownload(
                downloadItem = HttpDownloadItem(
                    id = -1,
                    link = updateDirectDownloadLink.link,
                    folder = updateDownloadsFolder,
                    name = updateDirectDownloadLink.name,
                ),
                onDuplicateStrategy = OnDuplicateStrategy.AddNumbered,
                queueId = null,
                categoryId = null,
            )
        coroutineScope {
            if (requireRestartDownload) {
                downloadSystem.reset(id)
            }
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

    override suspend fun removeUpdate(updateDirectDownloadLink: UpdateSource.DirectDownloadLink) {
        val id = downloadSystem
            .getDownloadItemsByFolder(updateDownloadLocationProvider.getSaveLocation().path)
            .find { it.name == updateDirectDownloadLink.name }?.id
        id?.let {
            downloadSystem.removeDownload(id, true, EmptyContext)
        }
    }

    override suspend fun removeAllUpdates() {
        val ids = downloadSystem
            .getDownloadItemsByFolder(updateDownloadLocationProvider.getSaveLocation().path)
            .map { it.id }
        for (id in ids) {
            downloadSystem.removeDownload(
                id = id, alsoRemoveFile = true, EmptyContext
            )
        }
    }
}
