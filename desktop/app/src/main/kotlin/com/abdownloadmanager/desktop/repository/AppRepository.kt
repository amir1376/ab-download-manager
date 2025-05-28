package com.abdownloadmanager.desktop.repository

import ir.amirab.util.datasize.CommonSizeConvertConfigs
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.utils.AutoStartManager
import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.downloader.DownloadSettings
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import com.abdownloadmanager.shared.utils.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.utils.category.CategoryManager
import com.abdownloadmanager.shared.utils.proxy.ProxyManager
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.util.datasize.BaseSize
import ir.amirab.util.datasize.ConvertSizeConfig
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.withPrevious
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppRepository : KoinComponent {
    private val scope: CoroutineScope by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val proxyManager: ProxyManager by inject()
    val theme = appSettings.theme

    val uiScale = appSettings.uiScale
    private val downloadSystem: DownloadSystem by inject()
    private val downloadSettings: DownloadSettings by inject()
    private val downloadManager: DownloadManager = downloadSystem.downloadManager
    private val downloadMonitor: IDownloadMonitor = downloadSystem.downloadMonitor
    private val integration: Integration by inject()
    private val removedDownloadsFromDiskTracker: RemovedDownloadsFromDiskTracker by inject()
    val categoryManager: CategoryManager by inject()

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
    val sizeUnit = MutableStateFlow(
        CommonSizeConvertConfigs.BinaryBytes
    )
    val speedUnit = appSettings.useBitsForSpeed.mapStateFlow { useBits ->
        if (useBits) {
            CommonSizeConvertConfigs.BinaryBits
        } else {
            CommonSizeConvertConfigs.BinaryBytes
        }
    }

    fun setSpeedUnit(speedUnit: ConvertSizeConfig) {
        appSettings.useBitsForSpeed.value = speedUnit.baseSize == BaseSize.Bits
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
        integrationPort
            .debounce(500)
            .onEach {
                if (integrationEnabled.value) {
                    integration.enable(it)
                }
            }.launchIn(scope)
        integrationEnabled
            .debounce(500)
            .onEach { isEnabled ->
                if (isEnabled) {
                    integration.enable(integrationPort.value)
                } else {
                    integration.disable()
                }
            }.launchIn(scope)
        integration.integrationStatus.onEach { result ->
            //if there is an error in connection disable integration
            if (result is IntegrationResult.Fail) {
                integrationEnabled.update { false }
            }
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
    }

}
