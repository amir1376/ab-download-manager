package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.utils.AppVersion
import com.abdownloadmanager.desktop.utils.BaseComponent
import com.abdownloadmanager.utils.DownloadSystem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.DownloadItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.abdownloadmanager.updateapplier.JavaUpdateApplier
import com.abdownloadmanager.updateapplier.UpdateDownloader
import com.abdownloadmanager.updatechecker.UpdateChecker
import com.abdownloadmanager.updatechecker.VersionData
import java.io.File

sealed interface UpdateStatus {
    data object IDLE : UpdateStatus
    data object NoUpdate : UpdateStatus
    data object NewUpdate : UpdateStatus
    data class Error(val e: Throwable) : UpdateStatus
    data object Checking : UpdateStatus
}

class UpdateComponent(
    ctx: ComponentContext,
) : BaseComponent(
    ctx
),
    KoinComponent {
    private val updateChecker: UpdateChecker by inject()
    //maybe create it via DI
//    private val updateApplier: UpdateApplier by inject()

    private val downloadSystem: DownloadSystem by inject()

    val currentVersion = AppVersion.get()
    val showNewUpdate = MutableStateFlow(false)
    val newVersionData = MutableStateFlow(null as VersionData?)
    private val appSettings: AppSettingsStorage by inject()
    private var updateApplierJob: Job? = null

    var updateCheckStatus by mutableStateOf<UpdateStatus>(UpdateStatus.IDLE)

    fun performUpdate() {
        val versionData = newVersionData.value ?: error("there is no new version!")
        val updateApplier = JavaUpdateApplier(
            versionData,
            UpdateDownloaderViaDownloadSystem(
                downloadSystem,
                appSettings.defaultDownloadFolder.value,
                name = versionData.name
            )
        )
        updateApplierJob?.cancel()
        updateApplierJob = scope.launch {
            updateApplier.applyUpdate()
        }
    }

    fun showNewUpdate(versionData: VersionData) {
        newVersionData.update { versionData }
        showNewUpdate.update { true }
    }

    fun requestCheckForUpdate() {
        scope.launch {
            try {
                updateCheckStatus = UpdateStatus.Checking
                val result = updateChecker.check()
                if (result != null) {
                    showNewUpdate(result)
                    updateCheckStatus = UpdateStatus.NewUpdate
                } else {
                    updateCheckStatus = UpdateStatus.NoUpdate
                }
                updateCheckStatus = UpdateStatus.IDLE
            }catch (e:Exception){
                updateCheckStatus = UpdateStatus.Error(e)
            }
        }
    }

    fun requestClose() {
        showNewUpdate.update { false }
    }
}

class UpdateDownloaderViaDownloadSystem(
    private val downloadSystem: DownloadSystem,
    private val saveFolder: String,
    private val name: String,
) : UpdateDownloader,
    KoinComponent {
    override suspend fun download(link: String): File {
        val id = downloadSystem.getOrCreateDownloadByLink(
            DownloadItem(
                id = -1,
                link = link,
                folder = saveFolder,
                name = name,
            )
        )
        val downloaded = coroutineScope {
            val waiter = async {
                downloadSystem.downloadMonitor.waitForDownloadToFinishOrCancel(id)
            }
            downloadSystem.manualResume(id)
            waiter.await()
        }
        if (!downloaded) {
            error("Download Cancelled")
        }
        // we recheck download info maybe some dude change the file name!
        val downloadedItem = downloadSystem.getDownloadItemById(id)
        requireNotNull(downloadedItem) {
            "Download is removed!"
        }
        return downloadSystem.getDownloadFile(downloadedItem)
    }

}