package com.abdownloadmanager.android.repository

import com.abdownloadmanager.android.pages.browser.BrowserActivity
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import ir.amirab.downloader.DownloadSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppRepository(
    scope: CoroutineScope,
    appSettings: AppSettingsStorage,
    proxyManager: ProxyManager,
    downloadSystem: DownloadSystem,
    downloadSettings: DownloadSettings,
    removedDownloadsFromDiskTracker: RemovedDownloadsFromDiskTracker,
    categoryManager: CategoryManager,
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
        appSettings.browserIconInLauncher
            .debounce(500)
            .distinctUntilChanged()
            .onEach { enabled ->
                BrowserActivity.Companion.Launcher.setEnabled(enabled)
            }.launchIn(scope)
    }
}
