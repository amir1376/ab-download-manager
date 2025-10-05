package com.abdownloadmanager.desktop.pages.editdownload

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.shared.utils.DownloadSystem
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface EditDownloadPageEffects {
    data object BringToFront : EditDownloadPageEffects
}

class EditDownloadComponent(
    ctx: ComponentContext,
    val onRequestClose: () -> Unit,
    val downloadId: Long,
    val acceptEdit: StateFlow<Boolean>,
    private val onEdited: ((IDownloadItem) -> Unit) -> Unit,
) : BaseComponent(ctx),
    ContainsEffects<EditDownloadPageEffects> by supportEffects(),
    KoinComponent {
    private val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    val iconProvider: FileIconProvider by inject()
    val downloadSystem: DownloadSystem by inject()
    val editDownloadUiChecker =
        MutableStateFlow(null as EditDownloadInputs<IDownloadItem, IDownloadCredentials, *, *, *>?)

    init {
        scope.launch {
            load(downloadId)
        }
    }

    private var pendingCredential: IDownloadCredentials? = null
    private val _credentialsImportedFromExternal = MutableStateFlow(false)
    val credentialsImportedFromExternal = _credentialsImportedFromExternal.asStateFlow()
    fun importCredential(credentials: IDownloadCredentials) {
        editDownloadUiChecker.value?.let {
            it.importCredentials(credentials)
        } ?: run {
            pendingCredential = credentials
        }
        _credentialsImportedFromExternal.value = true
    }

    private suspend fun load(id: Long) {
        val downloadItem = downloadSystem.getDownloadItemById(id = id)
        if (downloadItem == null) {
            onRequestClose()
            println("item with id $id not found")
            return
        }
        val downloader = downloaderInUiRegistry.getDownloaderOf(downloadItem)
        if (downloader == null) {
            onRequestClose()
            println("downloader for id $id not found")
            return
        }
        val httpEditDownloadInputs = downloader.createEditDownloadInputs(
            currentDownloadItem = MutableStateFlow(downloadItem),
            editedDownloadItem = MutableStateFlow(downloadItem),
            conflictDetector = DownloadConflictDetector(downloadSystem),
            scope = scope,
        )
        editDownloadUiChecker.value = httpEditDownloadInputs
        pendingCredential?.let { credentials ->
            httpEditDownloadInputs.importCredentials(credentials)
            pendingCredential = null
        }
    }


    fun onRequestEdit() {
        if (!acceptEdit.value) {
            return
        }
        editDownloadUiChecker.value?.let { editDownloadUiChecker ->
            onEdited(editDownloadUiChecker::applyEditedItemTo)
        }
    }

    fun bringToFront() {
        sendEffect(EditDownloadPageEffects.BringToFront)
    }
}
