package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.shared.utils.DownloadSystem
import ir.amirab.util.desktop.keepawake.KeepAwake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class KeepAwakeManager(
    private val keepAwake: KeepAwake,
    private val downloadSystem: DownloadSystem,
    private val scope: CoroutineScope,
) {
    var job: Job? = null

    fun boot() {
        enable()
    }

    @Synchronized
    fun enable() {
        job?.cancel()
        job = downloadSystem.downloadMonitor
            .activeDownloadCount
            .map { it > 0 }
            .distinctUntilChanged()
            .onEach { isDownloadsActive ->
                if (isDownloadsActive) {
                    keepAwake.keepAwake()
                } else {
                    keepAwake.allowSleep()
                }
            }.launchIn(scope)
    }

    @Synchronized
    fun disable() {
        job?.cancel()
    }
}
