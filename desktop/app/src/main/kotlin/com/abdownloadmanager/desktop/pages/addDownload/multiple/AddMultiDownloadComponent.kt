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
import com.abdownloadmanager.desktop.pages.addDownload.multiple.AddMultiItemSaveMode.*
import com.abdownloadmanager.desktop.utils.asState
import com.abdownloadmanager.utils.FileIconProvider
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.category.CategoryItem
import com.abdownloadmanager.utils.category.CategoryManager
import com.abdownloadmanager.utils.category.CategorySelectionMode
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
    private val onRequestAddCategory: () -> Unit,
) : AddDownloadComponent(ctx, id),
    KoinComponent {

    val tableState = TableState(
        cells = AddMultiItemTableCells.all(),
        forceVisibleCells = listOf(
            AddMultiItemTableCells.Check,
            AddMultiItemTableCells.Name,
        )
    )
    private val appSettings by inject<AppRepository>()
    private val client by inject<DownloaderClient>()
    val downloadSystem by inject<DownloadSystem>()
    val fileIconProvider: FileIconProvider by inject()


    private val _folder = MutableStateFlow(appSettings.saveLocation.value)
    val folder = _folder.asStateFlow()
    fun setFolder(folder: String) {
        this._folder.update { folder }
        list.forEach {
            it.folder.update { folder }
        }
    }

    // when we select all files in one location let user option to auto categorize items
    private val _alsoAutoCategorize = MutableStateFlow(true)
    val alsoAutoCategorize = _alsoAutoCategorize.asStateFlow()
    fun setAlsoAutoCategorize(value: Boolean) {
        _alsoAutoCategorize.update { value }
    }


    private val categoryManager: CategoryManager by inject()
    val categories = categoryManager.categoriesFlow
    private val _selectedCategory = MutableStateFlow(categories.value.firstOrNull())
    val selectedCategory = _selectedCategory.asStateFlow()

    fun setSelectedCategory(category: Category) {
        _selectedCategory.update {
            category
        }
    }

    fun requestAddCategory() {
        onRequestAddCategory()
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
    private val _saveMode = MutableStateFlow(EachFileInTheirOwnCategory)
    val saveMode = _saveMode.asStateFlow()
    fun setSaveMode(saveMode: AddMultiItemSaveMode) {
        _saveMode.update { saveMode }
    }


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

    val isCategoryModeHasValidState by run {
        val category by selectedCategory.asState(scope)
        val saveMode by saveMode.asState(scope)
        derivedStateOf {
            when (saveMode) {
                EachFileInTheirOwnCategory -> true
                AllInOneCategory -> category != null
                InSameLocation -> true
            }
        }
    }
    val canClickAdd by derivedStateOf {
        selectionList.isNotEmpty() && isCategoryModeHasValidState
    }
    private val queueManager: QueueManager by inject()
    val queueList = queueManager.queues

    private fun getFolderForItem(
        categorySelectionMode: CategorySelectionMode?,
        url: String,
        fleName: String,
        defaultFolder: String,
    ): String {
        return when (categorySelectionMode) {
            CategorySelectionMode.Auto -> {
                downloadSystem.categoryManager
                    .getCategoryOf(
                        CategoryItem(
                            url = url,
                            fileName = fleName,
                        )
                    )?.getDownloadPath()
                    ?: defaultFolder
            }

            is CategorySelectionMode.Fixed -> {
                downloadSystem.categoryManager
                    .getCategoryById(categorySelectionMode.categoryId)?.getDownloadPath()
                    ?: defaultFolder
            }

            null -> defaultFolder
        }
    }

    fun requestAddDownloads(
        queueId: Long?,
    ) {
        val categorySelectionMode = when (saveMode.value) {
            EachFileInTheirOwnCategory -> CategorySelectionMode.Auto
            AllInOneCategory -> selectedCategory.value?.let {
                CategorySelectionMode.Fixed(it.id)
            }

            InSameLocation -> {
                if (alsoAutoCategorize.value) CategorySelectionMode.Auto
                else null
            }
        }
        val itemsToAdd = list
            .filter { it.credentials.value.link in selectionList }
            .filter {
                it.canAdd.value
                        || it.isDuplicate.value // we add numbered file strategy
            }
            .map {
                DownloadItem(
                    id = -1,
                    folder = getFolderForItem(
                        categorySelectionMode = categorySelectionMode,
                        url = it.credentials.value.link,
                        fleName = it.name.value,
                        defaultFolder = it.folder.value
                    ),
                    name = it.name.value,
                    link = it.credentials.value.link,
                    contentLength = it.length.value ?: -1,
                )
            }
        consumeDialog {
            onRequestAdd(
                items = itemsToAdd,
                onDuplicateStrategy = { OnDuplicateStrategy.AddNumbered },
                queueId = queueId,
                categorySelectionMode = categorySelectionMode
            )
            val folder = folder.value
            if (saveMode.value == InSameLocation) {
                addToLastUsedLocations(folder)
            }
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
        queueId: Long?,
        categorySelectionMode: CategorySelectionMode?,
    )
}