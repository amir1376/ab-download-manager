package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import ir.amirab.downloader.destination.IncompleteFileUtil
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.desktop.progress.FileProgressHandle
import ir.amirab.util.desktop.progress.PlatformFileProgress

/**
 * Mirrors active downloads into Finder progress indicators.
 */
class FinderProgressTracker(
    private val appSettings: AppSettingsStorage,
) {
    private data class Tracked(val path: String, val handle: FileProgressHandle)

    private val tracked = mutableMapOf<Long, Tracked>()

    fun update(downloads: List<ProcessingDownloadItemState>) {
        val active = downloads.filter {
            it.status is DownloadJobStatus.IsActive &&
                !it.isWaiting &&
                it.contentLength > 0 &&
                it.progress > 0
        }
        val activeIds = active.mapTo(HashSet(active.size)) { it.id }

        active.forEach { item ->
            val path = item.outputFilePath()
            val existing = tracked[item.id]
            if (existing != null && existing.path == path) {
                existing.handle.update(item.progress.coerceAtMost(item.contentLength), item.contentLength)
            } else {
                existing?.handle?.end()
                val handle = PlatformFileProgress.start(path)
                if (handle == null) {
                    tracked.remove(item.id)
                } else {
                    tracked[item.id] = Tracked(path, handle)
                    handle.update(item.progress.coerceAtMost(item.contentLength), item.contentLength)
                }
            }
        }

        tracked.keys.filter { it !in activeIds }.forEach {
            tracked.remove(it)?.handle?.end()
        }
    }

    fun close() {
        tracked.values.forEach { it.handle.end() }
        tracked.clear()
    }

    private fun ProcessingDownloadItemState.outputFilePath(): String {
        val file = getFullPath()
        return if (appSettings.appendExtensionToIncompleteDownloads.value) {
            IncompleteFileUtil.addIncompleteIndicator(file, id).path
        } else {
            file.path
        }
    }
}
