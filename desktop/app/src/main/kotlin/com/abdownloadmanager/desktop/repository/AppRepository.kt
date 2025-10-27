package com.abdownloadmanager.desktop.repository

import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.DownloadSettings
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.util.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class AppRepository(
    scope: CoroutineScope,
    appSettings: BaseAppSettingsStorage,
    proxyManager: ProxyManager,
    downloadSystem: DownloadSystem,
    downloadSettings: DownloadSettings,
    removedDownloadsFromDiskTracker: RemovedDownloadsFromDiskTracker,
    categoryManager: CategoryManager,
    private val integration: Integration,
) : BaseAppRepository(
    scope = scope,
    appSettings = appSettings,
    proxyManager = proxyManager,
    downloadSystem = downloadSystem,
    downloadSettings = downloadSettings,
    removedDownloadsFromDiskTracker = removedDownloadsFromDiskTracker,
    categoryManager = categoryManager,
) {
    init {
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
    }
}
