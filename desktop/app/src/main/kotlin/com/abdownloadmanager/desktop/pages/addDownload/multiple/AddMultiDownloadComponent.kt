package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.DownloadUiChecker
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.ui.widget.customtable.TableState
import com.abdownloadmanager.desktop.utils.DownloadSystem
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddMultiDownloadComponent(
    ctx: ComponentContext,
    id: String,
    private val onRequestClose: () -> Unit,
    private val onRequestAdd: OnRequestAdd,
) : AddDownloadComponent(ctx, id),
    KoinComponent {

    val tableState= TableState(
        cells = AddMultiItemTableCells.all(),
        forceVisibleCells = listOf(
            AddMultiItemTableCells.Check,
            AddMultiItemTableCells.Name,
        )
    )
    private val appSettings by inject<AppRepository>()
    private val client by inject<DownloaderClient>()
    val downloadSystem by inject<DownloadSystem>()
    private val _folder=MutableStateFlow(appSettings.saveLocation.value)
    val folder = _folder.asStateFlow()
    fun setFolder(folder:String) {
        this._folder.update { folder }
        list.forEach {
            it.folder.update { folder }
        }
    }

    private fun newChecker(iDownloadCredentials: DownloadCredentials) = DownloadUiChecker(
        initialCredentials = iDownloadCredentials,
        initialName = "",
        initialFolder = folder.value,
        downloaderClient = client,
        downloadSystem = downloadSystem,
        scope = scope,
    )

    fun addItems(list: List<DownloadCredentials>) {
        val newItemsToAdd = list.filter {
            it !in this.list.map {
                it.credentials.value
            }
        }.map {
            newChecker(it)
        }
        enqueueCheck(newItemsToAdd)
        this.list = this.list.plus(newItemsToAdd)
    }

    var list: List<DownloadUiChecker> by mutableStateOf(emptyList())

    private val checkList = MutableSharedFlow<DownloadUiChecker>()
    private fun enqueueCheck(links: List<DownloadUiChecker>) {
        scope.launch {
            for (i in links) {
                checkList.emit(i)
            }
        }
    }

    init {
        checkList.onEach {
            it.refresh()
        }
            .launchIn(scope)
    }

    var selectionList by mutableStateOf<List<String>>(emptyList())
    fun isSelected(item: DownloadUiChecker): Boolean {
        return item.credentials.value.link in selectionList
    }

    val isAllSelected by derivedStateOf {
        list.all { it.credentials.value.link in selectionList }
    }

    var lastSelectedId by mutableStateOf(null as String?)

    fun setSelect(id: String, selected: Boolean) {
        if (selected) {
            lastSelectedId = id
            if (!selectionList.contains(id)) {
                selectionList = selectionList.plus(id)
            }
        } else {
            selectionList = selectionList.minus(id)
        }
    }

    fun resetSelectionTo(ids: List<String>, boolean: Boolean) {
        selectionList = ids.takeIf { boolean }
            .orEmpty()
    }

    fun selectAll(value: Boolean) {
        selectionList = if (value) {
            list.map { it.credentials.value.link }
        } else {
            emptyList()
        }
    }

    val canClickAdd by derivedStateOf {
        selectionList.isNotEmpty()
    }
    private val queueManager: QueueManager by inject()
    val queueList = queueManager.queues

    fun requestAddDownloads(
        queueId: Long?
    ) {
        val itemsToAdd = list
            .filter { it.credentials.value.link in selectionList }
            .filter {
                it.canAdd.value
                        || it.isDuplicate.value // we add numbered file strategy
            }
            .map {
                DownloadItem(
                    id = -1,
                    folder = it.folder.value,
                    name = it.name.value,
                    link = it.credentials.value.link,
                    contentLength = it.length.value ?: -1,
                )
            }
        consumeDialog {
            onRequestAdd(
                items = itemsToAdd,
                onDuplicateStrategy = { OnDuplicateStrategy.AddNumbered },
                queueId = queueId
            )
            addToLastUsedLocations(folder.value)
            requestClose()
        }
    }

    var showAddToQueue by mutableStateOf(false)
        private set

    fun openAddToQueueDialog() {
        showAddToQueue = true
    }

    fun closeAddToQueue() {
        showAddToQueue = false
    }

    fun requestClose() {
        onRequestClose()
    }
}

fun interface OnRequestAdd {
    operator fun invoke(
        items: List<DownloadItem>,
        onDuplicateStrategy: (DownloadItem) -> OnDuplicateStrategy,
        queueId: Long?
    )
}