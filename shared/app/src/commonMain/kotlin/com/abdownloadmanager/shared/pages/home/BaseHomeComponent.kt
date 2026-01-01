package com.abdownloadmanager.shared.pages.home

import androidx.compose.runtime.snapshotFlow
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.AddDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pagemanager.DownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EnterNewURLDialogManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.home.category.DefinedStatusCategories
import com.abdownloadmanager.shared.pages.home.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.shared.pages.home.queue.QueueActions
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryItemWithId
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.contexts.RemovedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.queue.queueModelsFlow
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.coroutines.combine
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

abstract class BaseHomeComponent(
    componentContext: ComponentContext,
    protected val downloadItemOpener: DownloadItemOpener,
    protected val downloadDialogManager: DownloadDialogManager,
    protected val editDownloadDialogManager: EditDownloadDialogManager,
    protected val addDownloadDialogManager: AddDownloadDialogManager,
    protected val fileChecksumDialogManager: FileChecksumDialogManager,
    protected val queuePageManager: QueuePageManager,
    protected val categoryDialogManager: CategoryDialogManager,
    protected val notificationSender: NotificationSender,
    protected val downloadSystem: DownloadSystem,
    val categoryManager: CategoryManager,
    val queueManager: QueueManager,
    protected val defaultCategories: DefaultCategories,
    val fileIconProvider: FileIconProvider,
) : BaseComponent(componentContext),
    ContainsEffects<BaseHomeComponent.Effects> by supportEffects() {
    protected abstract val enterNewURLDialogManager: EnterNewURLDialogManager
    val filterState = FilterState()

    protected fun requestDelete(
        downloadList: List<Long>,
    ) {
        if (downloadList.isEmpty()) {
            // nothing to delete!
            return
        }
        scope.launch {
            val unfinished = downloadSystem.getUnfinishedDownloadIds()
                .count {
                    it in downloadList
                }
            val finished = downloadSystem.getFinishedDownloadIds()
                .count {
                    it in downloadList
                }
            sendEffect(
                Effects.Common.DeleteItems(
                    list = downloadList,
                    unfinishedCount = unfinished,
                    finishedCount = finished,
                )
            )
        }
    }

    fun onConfirmDeleteCategory(promptState: CategoryDeletePromptState) {
        scope.launch {
            categoryManager.deleteCategory(promptState.category)
        }
    }

    fun confirmDelete(promptState: DeletePromptState) {
        scope.launch {
            val selectionList = promptState.downloadList
            for (id in selectionList) {
                downloadSystem.removeDownload(
                    id = id,
                    alsoRemoveFile = promptState.alsoDeleteFile,
                    context = RemovedBy(User),
                )
            }
        }
    }

    fun onConfirmAutoCategorize() {
        val categorizedItems = categoryManager.getCategories()
            .flatMap { it.items }
        val allDownloads = activeDownloadList.value + completedList.value
        val unCategorizedItems = allDownloads.filterNot {
            it.id in categorizedItems
        }
        categoryManager
            .autoAddItemsToCategoriesBasedOnFileNames(
                unCategorizedItems.map {
                    CategoryItemWithId(
                        id = it.id,
                        fileName = it.name,
                        url = it.downloadLink,
                    )
                }
            )
    }

    fun onConfirmResetCategories() {
        scope.launch {
            categoryManager.reset()
        }
    }

    fun moveItemsToCategory(category: Category, items: List<Long>) {
        scope.launch {
            categoryManager.addItemsToCategory(category.id, items)
        }
    }

    fun moveItemsToQueue(queue: DownloadQueue, items: List<Long>) {
        scope.launch {
            queueManager.addToQueue(queue.id, items)
        }
    }


    fun requestAddNewDownload(
        link: List<AddDownloadCredentialsInUiProps>,
    ) {
        addDownloadDialogManager.openAddDownloadDialog(link)
    }

    private val _selectionList = MutableStateFlow<List<Long>>(emptyList())
    val selectionList = _selectionList.asStateFlow()

    fun clearSelection() {
        _selectionList.update { emptyList() }
    }

    fun selectAll() {
        newSelection(
            ids = downloadList.value.map { it.id }
        )
    }

    fun newSelection(
        ids: List<Long>,
    ) {
        _selectionList.update { ids }
    }

    fun onItemSelectionChange(id: Long, checked: Boolean) {
        _selectionList.update { lastSelection ->
            if (checked) {
                if (!lastSelection.contains(id)) {
                    lastSelection + id
                } else {
                    lastSelection
                }
            } else {
                lastSelection - id
            }
        }

    }

    fun onCategoryFilterChange(
        statusCategoryFilter: DownloadStatusCategoryFilter,
        typeCategoryFilter: Category?,
    ) {
        this.filterState.queueFilter = null
        this.filterState.statusFilter = statusCategoryFilter
        this.filterState.typeCategoryFilter = typeCategoryFilter
    }

    fun onQueueFilterChange(
        queueModel: QueueModel
    ) {
        this.filterState.statusFilter = DefinedStatusCategories.All
        this.filterState.typeCategoryFilter = null
        this.filterState.queueFilter = queueModel
    }


    val activeDownloadCountFlow = downloadSystem.downloadMonitor.activeDownloadCount
    val globalSpeedFlow = downloadSystem.downloadMonitor.activeDownloadListFlow.map {
        it.sumOf { it.speed }
    }


    val activeDownloadList = downloadSystem.downloadMonitor.activeDownloadListFlow
    val completedList = downloadSystem.downloadMonitor.completedDownloadListFlow

    init {
        categoryManager.categoriesFlow.onEach { categories ->
            val currentCategory = filterState.typeCategoryFilter ?: return@onEach
            filterState.typeCategoryFilter = categories.find {
                it.id == currentCategory.id
            }
        }.launchIn(scope)
        queueManager.queueModelsFlow().onEach { queueModels ->
            val currentQueueModel = filterState.queueFilter ?: return@onEach
            filterState.queueFilter = queueModels.find {
                it.id == currentQueueModel.id
            }
        }.launchIn(scope)
    }

    val downloadList = combine(
        snapshotFlow { filterState.textToSearch },
        activeDownloadList,
        completedList,
        snapshotFlow { filterState.typeCategoryFilter },
        snapshotFlow { filterState.statusFilter },
        snapshotFlow { filterState.queueFilter },
    ) { textToSearch, activeDownloads, completeDownloads, categoryFilter, statusFilter, queueFilter ->
        val isSearching = textToSearch.isNotBlank()
        val allowedList = categoryFilter?.items ?: queueFilter?.queueItems
        (activeDownloads + completeDownloads)
            .filter {
                val statusAccepted = filterState.statusFilter.accept(it)
                val itemIsInAllowedList = allowedList?.contains(it.id) ?: true
                val searchAccepted = if (isSearching) {
                    it.name.contains(filterState.textToSearch, ignoreCase = true)
                } else true
                itemIsInAllowedList && statusAccepted && searchAccepted
            }
            // when restart a completed download item there is a duplication in list
            // so make sure to not pass bad data to download list table as it has item.id as key
            .distinctBy { it.id }
    }
        .withResumedLifecycle()
        .stateIn(scope, SharingStarted.Companion.Eagerly, emptyList())


    init {
        downloadList.onEach { downloads ->
            _selectionList.value = selectionList.value.filter { previouslySelectedItem ->
                downloads.any { it.id == previouslySelectedItem }
            }
        }.launchIn(scope)

        downloadSystem.downloadManager.listOfJobsEvents
            .filterIsInstance<DownloadManagerEvents.OnJobAdded>()
            // wait until download list in table is also updated
            // it also prevents extra emits when multiple download added at the same time
            .debounce(100)
            .onEach {
                sendEffect(Effects.Common.ScrollToDownloadItem(it.downloadItem.id))
            }.launchIn(scope)

    }


    protected val selectionListItems = combineStateFlows(
        selectionList,
        downloadList,
    ) { selectionList, downloadList ->
        val ids = selectionList
        ids.mapNotNull { id ->
            downloadList.find {
                it.id == id
            }
        }
    }

    fun openFileOrShowProperties(id: Long) {
        scope.launch {
            val dItem = downloadSystem.getDownloadItemById(id) ?: return@launch
            if (dItem.status != DownloadStatus.Completed) {
                downloadDialogManager.openDownloadDialog(id)
                return@launch
            }
            downloadItemOpener.openDownloadItem(dItem)
        }
    }

    fun openFile(id: Long) {
        scope.launch {
            val dItem = downloadSystem.getDownloadItemById(id) ?: return@launch
            if (dItem.status != DownloadStatus.Completed) {
                notificationSender.sendNotification(
                    Res.string.open_file,
                    Res.string.cant_open_file.asStringSource(),
                    Res.string.not_finished.asStringSource(),
                    NotificationType.Error,
                )
                return@launch
            }
            downloadItemOpener.openDownloadItem(dItem)
        }
    }

    val queueActions = MutableStateFlow(null as QueueActions?)

    fun showCategoryOptions(queue: DownloadQueue?) {
        queueActions.value = QueueActions(
            scope = scope,
            queueManager = queueManager,
            mainQueueModel = queue?.queueModel?.value,
            requestDelete = { queueModel ->
                scope.launch {
                    downloadSystem.deleteQueue(queueModel.id)
                }
            },
            requestEdit = { queueModel ->
                runCatching { queueManager.getQueue(queueModel.id) }
                    // it shouldn't be happened however I add this
                    .getOrNull()?.let { q ->
                        queuePageManager.openQueues(q.id)
                    }
            },
            requestClearItems = {
                scope.launch {
                    runCatching {
                        queueManager.clearQueue(it.id)
                    }
                }
            },
            onRequestNewQueue = {
                queuePageManager.openNewQueueDialog()
            }
        )
    }

    fun closeQueueOptions() {
        queueActions.value = null
    }

    val categoryActions = MutableStateFlow(null as CategoryActions?)

    fun showCategoryOptions(categoryItem: Category?) {
        categoryActions.value = CategoryActions(
            scope = scope,
            categoryManager = categoryManager,
            defaultCategories = defaultCategories,
            categoryItem = categoryItem,
            openFolder = {
                runCatching {
                    it.getDownloadPath()?.let {
                        FileUtils.Companion.openFolder(File(it))
                    }
                }
            },
            onRequestAddCategory = {
                categoryDialogManager.openCategoryDialog(-1)
            },
            requestDelete = {
                sendEffect(
                    Effects.Common.DeleteCategory(it)
                )
            },
            requestEdit = {
                categoryDialogManager.openCategoryDialog(it.id)
            },
            onRequestCategorizeItems = {
                sendEffect(Effects.Common.AutoCategorize)
            },
            onRequestResetToDefaults = {
                sendEffect(Effects.Common.ResetCategoriesToDefault)
            }
        )
    }

    fun closeCategoryOptions() {
        categoryActions.value = null
    }

    fun requestEnterNewURL() {
        enterNewURLDialogManager.openEnterNewURLWindow()
    }
    sealed interface Effects {
        interface PlatformEffects : Effects

        sealed interface Common : Effects {
            data class DeleteItems(
                val list: List<Long>,
                val finishedCount: Int,
                val unfinishedCount: Int,
            ) : Common

            data class DeleteCategory(
                val category: Category,
            ) : Common

            data object ResetCategoriesToDefault : Common
            data object AutoCategorize : Common
            data class ScrollToDownloadItem(
                val downloadId: Long,
                val skipIfVisible: Boolean = false,
            ) : Common
        }
    }
}
