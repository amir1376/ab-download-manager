package com.abdownloadmanager.shared.pages.editdownload

import arrow.core.identity
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.pagemanager.DownloadErrorDialogManager
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class BaseEditDownloadComponent(
    ctx: ComponentContext,
    private val downloadErrorDialogManager: DownloadErrorDialogManager,
    private val downloaderInUiRegistry: DownloaderInUiRegistry,
    val iconProvider: FileIconProvider,
    val downloadSystem: DownloadSystem,
    val onRequestClose: () -> Unit,
    val downloadId: Long,
    val acceptEdit: StateFlow<Boolean>,
    private val onEdited: ((IDownloadItem) -> Unit, DownloadJobExtraConfig?) -> Unit,
) : BaseComponent(ctx) {

    val editDownloadInputsFlow =
        MutableStateFlow(null as EditDownloadInputs<IDownloadItem, IDownloadCredentials, *, *, *, *>?)

    private val _lastErrorReason = MutableStateFlow<DownloadErrorReason?>(null)
    val lastErrorReason = _lastErrorReason.asStateFlow()

    init {
        scope.launch {
            load(downloadId)
        }
    }

    private var pendingCredential: IDownloadCredentials? = null
    private val _credentialsImportedFromExternal = MutableStateFlow(false)
    val credentialsImportedFromExternal = _credentialsImportedFromExternal.asStateFlow()
    fun importCredential(credentials: IDownloadCredentials) {
        editDownloadInputsFlow.value?.let {
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
        val editDownloadInputs = downloader.createEditDownloadInputs(
            currentDownloadItem = MutableStateFlow(downloadItem),
            editedDownloadItem = MutableStateFlow(downloadItem),
            conflictDetector = DownloadConflictDetector(downloadSystem),
            scope = scope,
        )
        editDownloadInputsFlow.value = editDownloadInputs
        pendingCredential?.let { credentials ->
            editDownloadInputs.importCredentials(credentials)
            pendingCredential = null
        }
        editDownloadInputs.responseResult.onEach { result ->
            _lastErrorReason.value = result?.fold(
                onSuccess = { it.unsuccessFullException },
                onFailure = ::identity
            )?.let(downloadSystem.errorMapperRegistry::getReason)
        }.launchIn(scope)
    }

    fun openDownloadErrorDialog() {
        val editedDownloadItem = editDownloadInputsFlow.value?.editedDownloadItem?.value ?: return
        val lastError = lastErrorReason.value ?: return
        downloadErrorDialogManager.openDownloadErrorDialog(
            downloadItem = editedDownloadItem,
            reason = lastError,
        )
    }

    fun onRequestEdit() {
        if (!acceptEdit.value) {
            return
        }
        editDownloadInputsFlow.value?.let { editDownloadUiChecker ->
            onEdited(editDownloadUiChecker::applyEditedItemTo, editDownloadUiChecker.downloadJobConfig.value)
        }
    }
}
