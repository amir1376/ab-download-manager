package com.abdownloadmanager.desktop.pages.quickdownload

import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.DownloadSystem
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.monitor.IDownloadMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuickDownloadComponent(
    ctx: ComponentContext,
    private val downloadSystem: DownloadSystem,
    val downloadId: Long,
    val initialUrl: String,
    initialName: String,
    initialFolder: String,
    private val onFinish: () -> Unit,
) : BaseComponent(ctx) {

    val fileName = MutableStateFlow(initialName)
    val saveFolder = MutableStateFlow(initialFolder)
    val selectedQueueId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = MutableStateFlow<Long?>(null)

    private val _isFinalizing = MutableStateFlow(false)
    val isFinalizing: StateFlow<Boolean> = _isFinalizing.asStateFlow()

    fun updateFileName(name: String) {
        fileName.value = name
    }

    fun updateSaveFolder(folder: String) {
        saveFolder.value = folder
    }

    fun updateQueueId(id: Long?) {
        selectedQueueId.value = id
    }

    fun updateCategoryId(id: Long?) {
        selectedCategoryId.value = id
    }

    fun confirm() {
        scope.launch {
            _isFinalizing.value = true
            try {
                downloadSystem.setFinalDestination<com.abdownloadmanager.shared.storage.IExtraDownloadItemSettings>(
                    downloadId = downloadId,
                    finalName = fileName.value,
                    finalFolder = saveFolder.value,
                    queueId = selectedQueueId.value,
                    categoryId = selectedCategoryId.value,
                )
            } finally {
                _isFinalizing.value = false
            }
            onFinish()
        }
    }

    fun cancel() {
        scope.launch {
            downloadSystem.cancelQuickDownload(downloadId)
            onFinish()
        }
    }
}
