package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.DesktopDownloadDialogManager
import com.abdownloadmanager.desktop.pages.multipledownloads.MultipleDownloadsController
import com.abdownloadmanager.shared.action.createStopAllAction
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.action.AnAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


fun createDesktopStopAllAction(
    scope: CoroutineScope,
    downloadSystem: DownloadSystem,
    desktopDownloadDialogManager: DesktopDownloadDialogManager,
    activeQueuesFlow: StateFlow<List<DownloadQueue>>,
    multipleDownloads: MultipleDownloadsController<*>,
): AnAction {
    return createStopAllAction(
        scope = scope,
        downloadSystem = downloadSystem,
        activeQueuesFlow = activeQueuesFlow,
        hasPendingWorkFlow = multipleDownloads.running,
        stopPendingWork = multipleDownloads::stopAndJoin,
        extraJobs = {
            val activeDownloadIds = downloadSystem.downloadMonitor.activeDownloadListFlow.value.map { it.id }
            desktopDownloadDialogManager.closeDownloadDialog(activeDownloadIds)
        }
    )
}
