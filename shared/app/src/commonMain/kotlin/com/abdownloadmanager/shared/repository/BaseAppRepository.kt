package com.abdownloadmanager.shared.repository

import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import com.abdownloadmanager.shared.util.AutoStartManager
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.DownloadSettings
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.util.datasize.ConvertSizeConfig
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.withPrevious
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

open class BaseAppRepository(
    protected val scope: CoroutineScope,
    protected val appSettings: BaseAppSettingsStorage,
    protected val proxyManager: ProxyManager,
    protected val downloadSystem: DownloadSystem,
    protected val downloadSettings: DownloadSettings,
    protected val removedDownloadsFromDiskTracker: RemovedDownloadsFromDiskTracker,
    protected val categoryManager: CategoryManager,
) : SizeAndSpeedUnitProvider {
    val theme = appSettings.theme
    val uiScale = appSettings.uiScale
    private val downloadManager: DownloadManager = downloadSystem.downloadManager
    private val downloadMonitor: IDownloadMonitor = downloadSystem.downloadMonitor

    val maxConcurrentDownloads = appSettings.maxConcurrentDownloads
    val speedLimiter = appSettings.speedLimit
    val threadCount = appSettings.threadCount
    val dynamicPartCreation = appSettings.dynamicPartCreation
    val useServerLastModifiedTime = appSettings.useServerLastModifiedTime
    val appendExtensionToIncompleteDownloads = appSettings.appendExtensionToIncompleteDownloads
    val useSparseFileAllocation = appSettings.useSparseFileAllocation
    val maxDownloadRetryCount = appSettings.maxDownloadRetryCount
    val useAverageSpeed = appSettings.useAverageSpeed
    val saveLocation = appSettings.defaultDownloadFolder
    val integrationEnabled = appSettings.browserIntegrationEnabled
    val integrationPort = appSettings.browserIntegrationPort
    val trackDeletedFilesOnDisk = appSettings.trackDeletedFilesOnDisk

    override val sizeUnit = appSettings.sizeUnit.mapStateFlow {
        it.toConfig()
    }
    override val speedUnit = appSettings.speedUnit.mapStateFlow {
        it.toConfig()
    }


    fun setSizeUnit(sizeUnit: ConvertSizeConfig) {
        SupportedSizeUnits.Companion.fromConfig(sizeUnit)?.let {
            appSettings.sizeUnit.value = it
        }
    }

    fun setSpeedUnit(speedUnit: ConvertSizeConfig) {
        SupportedSizeUnits.Companion.fromConfig(speedUnit)?.let {
            appSettings.speedUnit.value = it
        }
    }

    fun boot() {
        updateDownloadSettings()
    }

    private fun updateDownloadSettings() {
        downloadSettings.defaultThreadCount = threadCount.value
        downloadSettings.dynamicPartCreationMode = dynamicPartCreation.value
        downloadSettings.useServerLastModifiedTime = useServerLastModifiedTime.value
        downloadSettings.appendExtensionToIncompleteDownloads = appendExtensionToIncompleteDownloads.value
        downloadSettings.useSparseFileAllocation = useSparseFileAllocation.value
        downloadSettings.maxDownloadRetryCount = maxDownloadRetryCount.value
        downloadSettings.globalSpeedLimit = speedLimiter.value
    }

    init {
        saveLocation
            .debounce(500)
            .withPrevious()
            .onEach { (oldDownloadFolder, newDownloadFolder) ->
                if (oldDownloadFolder == null) {
                    return@onEach
                }
                categoryManager.updateCategoryFoldersBasedOnDefaultDownloadFolder(
                    previousDownloadFolder = oldDownloadFolder,
                    currentDownloadFolder = newDownloadFolder,
                )
            }.launchIn(scope)
        //maybe its better to move this to another place
        appSettings.autoStartOnBoot
            .debounce(500)
            .onEach { enabled ->
                AutoStartManager.startOnBoot(enabled)
            }.launchIn(scope)
        speedLimiter
            .debounce(500)
            .onEach {
                downloadSettings.globalSpeedLimit = it
                downloadManager.limitGlobalSpeed(it)
            }.launchIn(scope)
        useAverageSpeed
            .debounce(500)
            .onEach {
                downloadMonitor.useAverageSpeed = it
            }.launchIn(scope)
        threadCount
            .debounce(500)
            .onEach {
                downloadSettings.defaultThreadCount = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        dynamicPartCreation
            .debounce(500)
            .onEach {
                downloadSettings.dynamicPartCreationMode = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        useServerLastModifiedTime
            .debounce(500)
            .onEach {
                downloadSettings.useServerLastModifiedTime = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        appendExtensionToIncompleteDownloads
            .debounce(500)
            .onEach {
                downloadSettings.appendExtensionToIncompleteDownloads = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        useSparseFileAllocation
            .debounce(500)
            .onEach {
                downloadSettings.useSparseFileAllocation = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        maxDownloadRetryCount
            .debounce(500)
            .onEach {
                downloadSettings.maxDownloadRetryCount = it
                downloadManager.reloadSetting()
            }.launchIn(scope)
        trackDeletedFilesOnDisk
            .debounce(500)
            .onEach { enabled ->
                if (enabled) {
                    removedDownloadsFromDiskTracker.removeDownloadsThatFilesAreMissing()
                    removedDownloadsFromDiskTracker.start()
                } else {
                    removedDownloadsFromDiskTracker.stop()
                }
            }.launchIn(scope)
        maxConcurrentDownloads
            .debounce(500)
            .onEach {
                downloadSystem.manualDownloadQueue.setMaxConcurrent(it)
            }.launchIn(scope)
    }
}
