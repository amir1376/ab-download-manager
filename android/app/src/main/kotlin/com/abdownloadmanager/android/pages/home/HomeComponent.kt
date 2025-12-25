package com.abdownloadmanager.android.pages.home

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import com.abdownloadmanager.android.pages.enterurl.AndroidEnterNewURLComponent
import com.abdownloadmanager.android.pages.home.sections.sort.DownloadSortBy
import com.abdownloadmanager.android.storage.HomePageStorage
import com.abdownloadmanager.android.util.AppInfo
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.action.createCheckForUpdateAction
import com.abdownloadmanager.shared.action.createDownloadFromClipboardAction
import com.abdownloadmanager.shared.action.createDummyExceptionAction
import com.abdownloadmanager.shared.action.createDummyMessageAction
import com.abdownloadmanager.shared.action.createNewDownloadAction
import com.abdownloadmanager.shared.action.createOpenAboutPage
import com.abdownloadmanager.shared.action.createOpenBatchDownloadAction
import com.abdownloadmanager.shared.action.createOpenOpenSourceThirdPartyLibrariesPage
import com.abdownloadmanager.shared.action.createOpenSettingsAction
import com.abdownloadmanager.shared.action.createOpenTranslatorsPageAction
import com.abdownloadmanager.shared.action.createPerHostSettingsPage
import com.abdownloadmanager.shared.action.createStartQueueGroupAction
import com.abdownloadmanager.shared.action.createStopAllAction
import com.abdownloadmanager.shared.action.createStopQueueGroupAction
import com.abdownloadmanager.shared.action.donate
import com.abdownloadmanager.shared.action.supportActionGroup
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pagemanager.AboutPageManager
import com.abdownloadmanager.shared.pagemanager.AddDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.BatchDownloadPageManager
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pagemanager.DownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EnterNewURLDialogManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pagemanager.OpenSourceLibrariesPageManager
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.pagemanager.SettingsPageManager
import com.abdownloadmanager.shared.pagemanager.TranslatorsPageManager
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.home.BaseHomeComponent
import com.abdownloadmanager.shared.pages.home.category.DefinedStatusCategories
import com.abdownloadmanager.shared.pages.home.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.shared.pages.updater.UpdateComponent
import com.abdownloadmanager.shared.ui.widget.sort.Sort
import com.abdownloadmanager.shared.ui.widget.sort.sorted
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.abdownloadmanager.shared.util.subscribeAsStateFlow
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import ir.amirab.SelectionUtil
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.queue.activeQueuesFlow
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.plus

class HomeComponent(
    componentContext: ComponentContext,
    downloadItemOpener: DownloadItemOpener,
    downloadDialogManager: DownloadDialogManager,
    editDownloadDialogManager: EditDownloadDialogManager,
    addDownloadDialogManager: AddDownloadDialogManager,
    fileChecksumDialogManager: FileChecksumDialogManager,
    queuePageManager: QueuePageManager,
    categoryDialogManager: CategoryDialogManager,
    notificationSender: NotificationSender,
    downloadSystem: DownloadSystem,
    categoryManager: CategoryManager,
    queueManager: QueueManager,
    openSourceLibrariesPageManager: OpenSourceLibrariesPageManager,
    translatorsPageManager: TranslatorsPageManager,
    settingsPageManager: SettingsPageManager,
    perHostSettingsPageManager: PerHostSettingsPageManager,
    aboutPageManager: AboutPageManager,
    batchDownloadPageManager: BatchDownloadPageManager,
    defaultCategories: DefaultCategories,
    fileIconProvider: FileIconProvider,
    downloaderInUiRegistry: DownloaderInUiRegistry,
    private val updateComponent: UpdateComponent,
    private val homePageStorage: HomePageStorage,
) : BaseHomeComponent(
    componentContext,
    downloadItemOpener,
    downloadDialogManager,
    editDownloadDialogManager,
    addDownloadDialogManager,
    fileChecksumDialogManager,
    queuePageManager,
    categoryDialogManager,
    notificationSender,
    downloadSystem,
    categoryManager,
    queueManager,
    defaultCategories,
    fileIconProvider,
), EnterNewURLDialogManager {
    private val enterNewLinkNavigation = SlotNavigation<AndroidEnterNewURLComponent.Config>()
    val enterNewLinkSlot = childSlot(
        source = enterNewLinkNavigation,
        serializer = null,
        key = "enterNewLinkSlot",
        childFactory = { configuration, context ->
            AndroidEnterNewURLComponent(
                ctx = context,
                config = configuration,
                downloaderInUiRegistry = downloaderInUiRegistry,
                onCloseRequest = {
                    closeEnterNewURLWindow()
                },
                onRequestFinished = {
                    addDownloadDialogManager.openAddDownloadDialog(
                        links = listOf(AddDownloadCredentialsInUiProps(it))
                    )
                }
            )
        }
    ).subscribeAsStateFlow()

    override fun closeEnterNewURLWindow() {
        scope.launch {
            enterNewLinkNavigation.dismiss()
        }
    }

    override fun openEnterNewURLWindow() {
        scope.launch {
            enterNewLinkNavigation.activate(AndroidEnterNewURLComponent.Config)
        }
    }

    val downloadActions = AndroidDownloadActions(
        scope = scope,
        downloadSystem = downloadSystem,
        downloadDialogManager = downloadDialogManager,
        editDownloadDialogManager = editDownloadDialogManager,
        fileChecksumDialogManager = fileChecksumDialogManager,
        selections = selectionListItems,
        mainItem = selectionList.mapStateFlow {
            if (it.size == 1) it[0]
            else null
        },
        queueManager = queueManager,
        categoryManager = categoryManager,
        openFile = ::openFile,
        requestDelete = ::requestDelete,
        onRequestShareFiles = ::shareFiles,
    )

    private fun shareFiles(finishedDownloads: List<CompletedDownloadItemState>) {
        finishedDownloads.mapNotNull {
            File(it.folder, it.name).takeIf { file -> file.exists() }
        }.takeIf { it.isNotEmpty() }?.let {
            sendEffect(Effects.ShareFiles(it))
        }

    }

    fun onItemClicked(itemState: IDownloadItemState) {
        scope.launch {
            if (itemState is ProcessingDownloadItemState) {
                toggleDownload(itemState)
                return@launch
            }
            downloadItemOpener.openDownloadItem(itemState.id)
        }
    }

    suspend fun toggleDownload(dItem: ProcessingDownloadItemState) {
        when (dItem.status) {
            is DownloadJobStatus.CanBeResumed -> {
                downloadSystem.manualResume(dItem.id)
            }

            is DownloadJobStatus.IsActive -> {
                downloadSystem.manualPause(dItem.id)
            }

            else -> {}
        }
    }

    private val _selectedSort = homePageStorage.sortBy
    val selectedSort = _selectedSort.asStateFlow()
    fun setSelectedSort(
        sort: Sort<DownloadSortBy>
    ) {
        if (sort.cell in possibleSorts) {
            _selectedSort.value = sort
        }
    }

    val filterMode = derivedStateOf {
        val queueFilter = filterState.queueFilter
        val statusFilter = filterState.statusFilter
        val categoryFilter = filterState.typeCategoryFilter
        if (queueFilter != null) {
            FilterMode.Queue(queueFilter)
        } else {
            FilterMode.Status(statusFilter, categoryFilter)
        }
    }

    val sortedDownloadList = combine(
        downloadList,
        selectedSort,
        snapshotFlow { filterMode.value },
    ) { downloadList, sortBy, filterMode ->
        when (filterMode) {
            is FilterMode.Status -> {
                sortBy.sorted(downloadList)
            }

            is FilterMode.Queue -> {
                filterMode.queue.queueItems.mapNotNull { id ->
                    downloadList.find { it.id == id }
                }
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun onRequestSelectInside() {
        SelectionUtil.toggleSelectInside(
            selectionList = selectionList.value,
            fullSortedList = sortedDownloadList.value,
            getId = {
                it.id
            }
        )?.let {
            newSelection(it)
        }
    }

    fun onRequestInvertSelection() {
        newSelection(
            SelectionUtil.invertSelection(
                selectionList = selectionList.value,
                all = sortedDownloadList.value,
                getId = { it.id }
            )
        )
    }

    val allStatuseFilters = DefinedStatusCategories.values()
    val currentStatusIndexInList by derivedStateOf {
        allStatuseFilters.indexOf(filterState.statusFilter)
    }

    fun switchToNewStatus(value: Int) {
        filterState.statusFilter = allStatuseFilters[
            value.coerceIn(allStatuseFilters.indices)
        ]
    }

    private val _isShowingSearch: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isShowingSearch = _isShowingSearch.asStateFlow()
    fun setIsShowingSearch(shown: Boolean) {
        if (!shown) {
            filterState.textToSearch = ""
        } else {
            closePopups()
        }
        _isShowingSearch.value = shown
    }

    private val currentActivePopup = MutableStateFlow<HomePopups?>(null)
    fun onOverlayClicked() {
        closePopups()
    }

    fun closePopups() {
        currentActivePopup.value = null
    }

    val isMainMenuShowing = currentActivePopup.mapStateFlow {
        it == HomePopups.MainMenu
    }

    fun setIsMainMenuShowing(value: Boolean) {
        currentActivePopup.value = HomePopups.MainMenu.takeIf { value }
    }

    val isCategoryFilterShowing = currentActivePopup.mapStateFlow {
        it == HomePopups.FilterMenu
    }

    fun setIsCategoryFilterShowing(value: Boolean) {
        currentActivePopup.value = HomePopups.FilterMenu.takeIf { value }
    }

    val isSortMenuShowing = currentActivePopup.mapStateFlow {
        it == HomePopups.SortMenu
    }

    fun setIsSortMenuShowing(value: Boolean) {
        currentActivePopup.value = HomePopups.SortMenu.takeIf { value }
    }

    val isAddMenuShowing = currentActivePopup.mapStateFlow {
        it == HomePopups.AddMenu
    }

    fun setIsAddMenuShowing(value: Boolean) {
        currentActivePopup.value = HomePopups.AddMenu.takeIf { value }
    }


    val activeQueuesFlow = queueManager.activeQueuesFlow(scope)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    val mainMenu = buildMenu {
        +createStopAllAction(scope, downloadSystem, {}, activeQueuesFlow)
        separator()
        subMenu(
            title = Res.string.delete.asStringSource(),
            icon = MyIcons.remove
        ) {
            item(Res.string.all_missing_files.asStringSource()) {
                requestDelete(downloadSystem.getListOfDownloadThatMissingFileOrHaveNotProgress().map { it.id })
            }
            item(Res.string.all_finished.asStringSource()) {
                requestDelete(downloadSystem.getFinishedDownloadIds())
            }
            item(Res.string.all_unfinished.asStringSource()) {
                requestDelete(downloadSystem.getUnfinishedDownloadIds())
            }
            item(Res.string.entire_list.asStringSource()) {
                requestDelete(downloadSystem.getAllDownloadIds())
            }
        }
        separator()
        +createStartQueueGroupAction(scope, queueManager)
        +createStopQueueGroupAction(scope, activeQueuesFlow)
        if (AppInfo.isInDebugMode) {
            separator()
            +createDummyMessageAction(notificationSender)
            +createDummyExceptionAction()
        }
        separator()
        +createPerHostSettingsPage(perHostSettingsPageManager = perHostSettingsPageManager)
        +createOpenSettingsAction(settingsPageManager = settingsPageManager)
        separator()
        subMenu(
            Res.string.help.asStringSource(),
            MyIcons.question,
        ) {
            +supportActionGroup
            separator()
            +createOpenOpenSourceThirdPartyLibrariesPage(openSourceLibrariesPageManager = openSourceLibrariesPageManager)
            +createOpenTranslatorsPageAction(opeTranslatorsPageManager = translatorsPageManager)
            +donate
            separator()
            +createCheckForUpdateAction(updateComponent)
            +createOpenAboutPage(aboutPageManager)
        }
    }
    val addMenu = buildMenu {
        +createDownloadFromClipboardAction(addDownloadDialogManager = addDownloadDialogManager)
        +createNewDownloadAction(enterNewURLDialogManager = enterNewURLDialogManager)
        +createOpenBatchDownloadAction(batchDownloadPageManager = batchDownloadPageManager)
    }

    val isOverlayVisible = currentActivePopup.mapStateFlow {
        it != null
    }

    val possibleSorts = listOf(
        DownloadSortBy.DataAdded,
        DownloadSortBy.Name,
        DownloadSortBy.Size,
        DownloadSortBy.Status,
    )

    fun startQueue(id: Long) {
        scope.launch {
            queueManager.getQueue(id).start()
        }
    }

    fun stopQueue(id: Long) {
        scope.launch {
            queueManager.getQueue(id).stop()
        }
    }

    private fun getCurrentDownloadQueue(): DownloadQueue? {
        val queueId = (filterMode.value as? FilterMode.Queue)?.queue?.id ?: return null
        return runCatching { queueManager.getQueue(queueId) }.getOrNull()
    }

    fun reorderQueueItemsUp() {
        val downloadQueue = getCurrentDownloadQueue() ?: return
        val itemsToMove = selectionList.value
        downloadQueue.moveUp(itemsToMove)
        val queueItems = downloadQueue.queueModel.value.queueItems
        val firstItemId = queueItems.firstOrNull { itemsToMove.contains(it) }
        firstItemId?.let {
            scope.launch {
                sendEffect(BaseHomeComponent.Effects.Common.ScrollToDownloadItem(it, true))
            }
        }
    }

    fun reorderQueueItemsDown() {
        val downloadQueue = getCurrentDownloadQueue() ?: return
        val itemsToMove = selectionList.value
        downloadQueue.moveDown(itemsToMove)
        val queueItems = downloadQueue.queueModel.value.queueItems
        val lastItemId = queueItems.lastOrNull { itemsToMove.contains(it) }
        lastItemId?.let {
            sendEffect(BaseHomeComponent.Effects.Common.ScrollToDownloadItem(it, true))
        }
    }

    fun reorderQueueItems(fromIndex: Int, toIndex: Int) {
        val downloadQueue = getCurrentDownloadQueue() ?: return
        val currentDraggingItem = runCatching {
            downloadQueue.getQueueItemFromOrder(fromIndex)
        }.getOrNull()
        val listOfIds = selectionList.value
            .let {
                if (currentDraggingItem != null && !it.contains(currentDraggingItem)) {
                    it.plus(currentDraggingItem)
                } else {
                    it
                }
            }

        val delta = toIndex - fromIndex
        downloadQueue.move(
            listOfIds, delta
        )
        val queueItems = downloadQueue.queueModel.value.queueItems
        val itemToScroll = if (delta > 0) {
            queueItems.lastOrNull { listOfIds.contains(it) }
        } else {
            queueItems.firstOrNull { listOfIds.contains(it) }
        }
        itemToScroll?.let {
            sendEffect(BaseHomeComponent.Effects.Common.ScrollToDownloadItem(it))
        }
    }

    fun removeQueueItems() {
        val downloadQueue = getCurrentDownloadQueue() ?: return
        val itemsToRemove = selectionList.value
        downloadQueue.removeFromQueue(itemsToRemove)
    }

    fun revealItem(downloadId: Long) {
        scope.launch {
            sendEffect(BaseHomeComponent.Effects.Common.ScrollToDownloadItem(downloadId))
        }
    }

    override val enterNewURLDialogManager: EnterNewURLDialogManager
        get() = this

    sealed interface FilterMode {
        data class Status(
            val downloadStatus: DownloadStatusCategoryFilter,
            val category: Category?,
        ) : FilterMode

        data class Queue(
            val queue: QueueModel,
        ) : FilterMode
    }

    sealed interface Effects : BaseHomeComponent.Effects.PlatformEffects {
        data class ShareFiles(
            val files: List<File>,
        ) : Effects
    }
}

sealed interface HomePopups {
    data object AddMenu : HomePopups
    data object MainMenu : HomePopups
    data object SortMenu : HomePopups
    data object FilterMenu : HomePopups
}
