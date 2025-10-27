package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.shared.ui.widget.table.customtable.TableState
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pages.adddownload.multiple.BaseAddMultiDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.multiple.OnRequestAdd
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.queue.QueueManager

class DesktopAddMultiDownloadComponent(
    ctx: ComponentContext,
    id: String,
    onRequestClose: () -> Unit,
    onRequestAdd: OnRequestAdd,
    private val categoryDialogManager: CategoryDialogManager,
    lastSavedLocationsStorage: ILastSavedLocationsStorage,
    perHostSettingsManager: PerHostSettingsManager, downloadSystem: DownloadSystem,
    fileIconProvider: FileIconProvider,
    appRepository: AppRepository,
    downloaderInUiRegistry: DownloaderInUiRegistry,
    queueManager: QueueManager,
    categoryManager: CategoryManager,
) : BaseAddMultiDownloadComponent(
    ctx = ctx,
    id = id,
    lastSavedLocationsStorage = lastSavedLocationsStorage,
    onRequestAdd = onRequestAdd,
    onRequestClose = onRequestClose,
    perHostSettingsManager = perHostSettingsManager,
    downloadSystem = downloadSystem,
    appRepository = appRepository,
    fileIconProvider = fileIconProvider,
    downloaderInUiRegistry = downloaderInUiRegistry,
    queueManager = queueManager,
    categoryManager = categoryManager,
) {
    override fun getCategoryPageManager(): CategoryDialogManager {
        return categoryDialogManager
    }
    val tableState = TableState(
        cells = AddMultiItemTableCells.all(),
        forceVisibleCells = listOf(
            AddMultiItemTableCells.Check,
            AddMultiItemTableCells.Name,
        )
    )
}

