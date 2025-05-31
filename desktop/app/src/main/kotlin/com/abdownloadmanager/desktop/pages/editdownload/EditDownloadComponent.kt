package com.abdownloadmanager.desktop.pages.editdownload

import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.shared.utils.DownloadSystem
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.applyFrom
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
    private val onEdited: ((DownloadItem) -> Unit) -> Unit,
) : BaseComponent(ctx),
    ContainsEffects<EditDownloadPageEffects> by supportEffects(),
    KoinComponent {
    private val downloaderClient: DownloaderClient by inject()
    val iconProvider: FileIconProvider by inject()
    val downloadSystem: DownloadSystem by inject()
    private val appRepository: AppRepository by inject()
    val editDownloadUiChecker = MutableStateFlow(null as EditDownloadState?)

    init {
        scope.launch {
            load(downloadId)
        }
    }

    private var pendingCredential: DownloadCredentials? = null
    private val _credentialsImportedFromExternal = MutableStateFlow(false)
    val credentialsImportedFromExternal = _credentialsImportedFromExternal.asStateFlow()
    fun importCredential(credentials: DownloadCredentials) {
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
        val editDownloadState = EditDownloadState(
            currentDownloadItem = MutableStateFlow(downloadItem),
            editedDownloadItem = MutableStateFlow(downloadItem),
            downloaderClient = downloaderClient,
            conflictDetector = object : DownloadConflictDetector {
                override fun checkAlreadyExists(current: DownloadItem, edited: DownloadItem): Boolean {
                    val editedDownloadFile = downloadSystem.getDownloadFile(edited)
                    val alreadyExists = editedDownloadFile.exists()
                    if (alreadyExists) {
                        return true
                    }
                    return downloadSystem
                        .getAllRegisteredDownloadFiles()
                        .contains(editedDownloadFile)
                }
            },
            scope = scope,
            appRepository = appRepository,
        )
        editDownloadUiChecker.value = editDownloadState
        pendingCredential?.let { credentials ->
            editDownloadState.importCredentials(credentials)
            pendingCredential = null
        }
    }


    fun onRequestEdit() {
        if (!acceptEdit.value) {
            return
        }
        editDownloadUiChecker.value?.let { editDownloadUiChecker ->
            onEdited {
                it.applyOurChanges(editDownloadUiChecker.editedDownloadItem.value)
            }
        }
    }

    fun bringToFront() {
        sendEffect(EditDownloadPageEffects.BringToFront)
    }

    private fun DownloadItem.applyOurChanges(edited: DownloadItem) {
        // we don't change some of these properties, so I commented them

        link = edited.link
        headers = edited.headers
        username = edited.username
        password = edited.password
        downloadPage = edited.downloadPage
        userAgent = edited.userAgent

//        id = edited.id
        folder = edited.folder
        name = edited.name

        contentLength = edited.contentLength
        serverETag = edited.serverETag

//        dateAdded = edited.dateAdded
//        startTime = edited.startTime
//        completeTime = edited.completeTime
//        status = edited.status
        preferredConnectionCount = edited.preferredConnectionCount
        speedLimit = edited.speedLimit

        fileChecksum = edited.fileChecksum
    }
}
