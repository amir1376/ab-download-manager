package com.abdownloadmanager.desktop.repository

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.utils.AutoStartManager
import com.abdownloadmanager.utils.DownloadSystem
import ir.amirab.downloader.DownloadSettings
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import com.abdownloadmanager.utils.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.utils.proxy.ProxyManager
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.monitor.IDownloadMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

    val speedLimiter = appSettings.speedLimit
    val threadCount = appSettings.threadCount
    val dynamicPartCreation = appSettings.dynamicPartCreation
    val useServerLastModifiedTime = appSettings.useServerLastModifiedTime
    val useSparseFileAllocation = appSettings.useSparseFileAllocation
    val useAverageSpeed = appSettings.useAverageSpeed
    val saveLocation = appSettings.defaultDownloadFolder
    val integrationEnabled = appSettings.browserIntegrationEnabled
    val integrationPort = appSettings.browserIntegrationPort
    val trackDeletedFilesOnDisk = appSettings.trackDeletedFilesOnDisk

    init {
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
        useSparseFileAllocation
            .debounce(500)
            .onEach {
                downloadSettings.useSparseFileAllocation = it
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