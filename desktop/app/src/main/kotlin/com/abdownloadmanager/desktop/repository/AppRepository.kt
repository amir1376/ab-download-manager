package com.abdownloadmanager.desktop.repository

import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.DownloadSettings
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import com.abdownloadmanager.integration.IntegrationSettings
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.util.ApiKeyUtil
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
        combine(
            apiEnabled,
            apiPort,
            apiAuthEnabled,
            apiAuthKey,
        ) { apiEnabled, apiPort, apiAuthEnabled, apiAuthKey ->
            if (!apiEnabled) {
                return@combine null
            }
            IntegrationSettings(
                port = apiPort,
                apiKey = apiAuthKey
                    .takeIf { apiAuthEnabled }
                    .takeIf { ApiKeyUtil.isValidKey(apiAuthKey) }
            )
        }
            .debounce(500)
            .onEach {
                if (it != null) {
                    integration.enable(it)
                } else {
                    integration.disable()
                }
            }.launchIn(scope)
        integration.integrationStatus.onEach { result ->
            //if there is an error in connection disable integration
            if (result is IntegrationResult.Fail) {
                apiEnabled.update { false }
            }
        }.launchIn(scope)
    }
}
