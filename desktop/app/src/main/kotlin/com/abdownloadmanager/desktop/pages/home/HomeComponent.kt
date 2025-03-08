package com.abdownloadmanager.desktop.pages.home

import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.desktop.*
import com.abdownloadmanager.desktop.actions.*
import com.abdownloadmanager.desktop.pages.category.CategoryDialogManager
import com.abdownloadmanager.desktop.pages.home.sections.DownloadListCells
import com.abdownloadmanager.desktop.pages.home.sections.category.DefinedStatusCategories
import com.abdownloadmanager.desktop.pages.home.sections.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.desktop.utils.DesktopShortcutManager
import com.abdownloadmanager.desktop.utils.isInDebugMode
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.ui.widget.customtable.Sort
import com.abdownloadmanager.shared.ui.widget.customtable.TableState
import com.abdownloadmanager.shared.utils.*
import com.abdownloadmanager.shared.utils.category.Category
import com.abdownloadmanager.shared.utils.category.CategoryItemWithId
import com.abdownloadmanager.shared.utils.category.CategoryManager
import com.abdownloadmanager.shared.utils.category.DefaultCategories
import com.abdownloadmanager.shared.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.contexts.RemovedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.isFinished
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.AppVersionTracker
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

@Stable
class FilterState {
    var textToSearch by mutableStateOf("")
    var typeCategoryFilter by mutableStateOf(null as Category?)
    var statusFilter by mutableStateOf<DownloadStatusCategoryFilter>(DefinedStatusCategories.All)
}

sealed interface HomeEffects {
    data object BringToFront : HomeEffects

    data class DeleteItems(
        val list: List<Long>,
        val finishedCount: Int,
        val unfinishedCount: Int,
    ) : HomeEffects

    data class DeleteCategory(
        val category: Category,
    ) : HomeEffects

    data object ResetCategoriesToDefault : HomeEffects
    data object AutoCategorize : HomeEffects
}


class DownloadActions(
    private val scope: CoroutineScope,
    downloadSystem: DownloadSystem,
    downloadDialogManager: DownloadDialogManager,
    editDownloadDialogManager: EditDownloadDialogManager,
    fileChecksumDialogManager: FileChecksumDialogManager,
    val selections: StateFlow<List<IDownloadItemState>>,
    private val mainItem: StateFlow<Long?>,
    private val queueManager: QueueManager,
    private val categoryManager: CategoryManager,
    private val openFile: (Long) -> Unit,
    private val openFolder: (Long) -> Unit,
    private val requestDelete: (List<Long>) -> Unit,
) {
    val defaultItem = combineStateFlows(
        selections,
        mainItem,
    ) { selections, mainItem ->
        selections.let {
            it.find {
                it.id == mainItem
            } ?: it.firstOrNull()
        }
    }
    val resumableSelections = selections.mapStateFlow {
        it.filter {
            it.statusOrFinished() is DownloadJobStatus.CanBeResumed
        }
    }
    val pausableSelections = selections.mapStateFlow {
        it.filter {
            it.statusOrFinished() is DownloadJobStatus.IsActive
        }
    }
    val openFileAction = simpleAction(
        title = Res.string.open.asStringSource(),
        icon = MyIcons.fileOpen,
        checkEnable = defaultItem.mapStateFlow {
            it?.statusOrFinished() is DownloadJobStatus.Finished
        },
        onActionPerformed = {
            scope.launch {
                val d = defaultItem.value ?: return@launch
                openFile(d.id)
            }
        }
    )

    val openFolderAction = simpleAction(
        title = Res.string.open_folder.asStringSource(),
        icon = MyIcons.folderOpen,
        checkEnable = defaultItem.mapStateFlow {
            it?.statusOrFinished() is DownloadJobStatus.Finished
        },
        onActionPerformed = {
            scope.launch {
                val d = defaultItem.value ?: return@launch
                openFolder(d.id)
            }
        }
    )

    val deleteAction = simpleAction(
        title = Res.string.delete.asStringSource(),
        icon = MyIcons.remove,
        checkEnable = selections.mapStateFlow { it.isNotEmpty() },
        onActionPerformed = {
            scope.launch {
                requestDelete(selections.value.map { it.id })
            }
        },
    )

    val resumeAction = simpleAction(
        title = Res.string.resume.asStringSource(),
        icon = MyIcons.resume,
        checkEnable = resumableSelections.mapStateFlow {
            it.isNotEmpty()
        },
        onActionPerformed = {
            scope.launch {
                resumableSelections.value.forEach {
                    runCatching {
                        downloadSystem.manualResume(it.id)
                    }
                }
            }
        }
    )

    val reDownloadAction = simpleAction(
        Res.string.restart_download.asStringSource(),
        MyIcons.refresh
    ) {
        scope.launch {
            selections.value.forEach {
                scope.launch {
                    runCatching {
                        downloadSystem.reset(it.id)
                        downloadSystem.manualResume(it.id)
                    }
                }
            }
        }
    }

    val pauseAction = simpleAction(
        title = Res.string.pause.asStringSource(),
        icon = MyIcons.pause,
        checkEnable = pausableSelections.mapStateFlow {
            it.isNotEmpty()
        },
        onActionPerformed = {
            scope.launch {
                pausableSelections.value.forEach {
                    runCatching {
                        downloadSystem.manualPause(it.id)
                    }
                }
            }
        }
    )
    val editDownloadAction = simpleAction(
        title = Res.string.edit.asStringSource(),
        icon = MyIcons.edit,
        checkEnable = defaultItem.mapStateFlow {
            it ?: return@mapStateFlow false
            it.statusOrFinished() !is DownloadJobStatus.IsActive
        },
        onActionPerformed = {
            scope.launch {
                val item = defaultItem.value ?: return@launch
                editDownloadDialogManager.openEditDownloadDialog(item.id)
            }
        }
    )

    val copyDownloadLinkAction = simpleAction(
        title = Res.string.copy_link.asStringSource(),
        icon = MyIcons.copy,
        checkEnable = selections.mapStateFlow { it.isNotEmpty() },
        onActionPerformed = {
            scope.launch {
                val credentialsList = selections.value
                    .mapNotNull { downloadSystem.getDownloadItemById(it.id) }
                    .map { DownloadCredentials.from(it) }
                ClipboardUtil.copy(generateCurlCommands(credentialsList).joinToString("\n"))
//                val json = Json.encodeToString(credentialsList)
//                ClipboardUtil.copy(json)
            }
        }
    )

    fun generateCurlCommands(credentialsList: List<DownloadCredentials>): List<String> {
        return credentialsList.map { credentials ->
            val curlCommand = StringBuilder("curl \"${credentials.link}\"")
            credentials.headers?.forEach { (headerName, headerValue) ->
                curlCommand.append(" -H \"${headerName}: ${headerValue}\"")
            }
            curlCommand.append(" -O")
            curlCommand.toString()
        }
    }

    val openDownloadDialogAction = simpleAction(Res.string.show_properties.asStringSource(), MyIcons.info) {
        selections.value.map { it.id }
            .forEach { id ->
                downloadDialogManager.openDownloadDialog(id)
            }
    }
    private val fileChecksumAction = simpleAction(
        title = Res.string.file_checksum.asStringSource(), MyIcons.info,
        checkEnable = selections.mapStateFlow { list ->
            list.any { iiDownloadItemState ->
                iiDownloadItemState.isFinished()
            }
        }
    ) {
        fileChecksumDialogManager.openFileChecksumPage(
            selections.value.map { it.id }
        )
    }

    private val moveToQueueItems = MenuItem.SubMenu(
        title = Res.string.move_to_queue.asStringSource(),
        items = emptyList()
    ).apply {
        merge(
            queueManager.queues,
            selections
        ).onEach {
            val qs = queueManager.queues.value
            val list = qs.map {
                moveToQueueAction(it, selections.value.map { it.id })
            }
            setItems(list)
        }.launchIn(scope)
    }
    private val moveToCategoryAction = MenuItem.SubMenu(
        title = Res.string.move_to_category.asStringSource(),
        items = emptyList()
    ).apply {
        merge(
            categoryManager.categoriesFlow.mapStateFlow {
                it.map(Category::id)
            },
            selections
        ).onEach {
            val categories = categoryManager.categoriesFlow.value
            val list = categories.map { category ->
                createMoveToCategoryAction(
                    category = category,
                    itemIds = selections.value.map { iDownloadItemState ->
                        iDownloadItemState.id
                    }
                )
            }
            setItems(list)
        }.launchIn(scope)
    }


    val menu: List<MenuItem> = buildMenu {
        +openFileAction
        +openFolderAction
        +(resumeAction)
        +pauseAction
        separator()
        +(deleteAction)
        +(reDownloadAction)
        separator()
        +moveToQueueItems
        +moveToCategoryAction
        separator()
        +(copyDownloadLinkAction)
        +editDownloadAction
        +fileChecksumAction
        +(openDownloadDialogAction)
    }
}

@Stable
class CategoryActions(
    private val scope: CoroutineScope,
    private val categoryManager: CategoryManager,
    private val defaultCategories: DefaultCategories,

    val categoryItem: Category?,

    private val openFolder: (Category) -> Unit,
    private val requestDelete: (Category) -> Unit,
    private val requestEdit: (Category) -> Unit,

    private val onRequestResetToDefaults: () -> Unit,
    private val onRequestCategorizeItems: () -> Unit,
    private val onRequestAddCategory: () -> Unit,
) {
    private val mainItemExists = MutableStateFlow(categoryItem != null)
    private val canBeOpened = MutableStateFlow(categoryItem?.usePath ?: false)
    private inline fun useItem(
        block: (Category) -> Unit,
    ) {
        categoryItem?.let(block)
    }

    val openCategoryFolderAction = simpleAction(
        title = Res.string.open_folder.asStringSource(),
        icon = MyIcons.folderOpen,
        checkEnable = canBeOpened,
        onActionPerformed = {
            scope.launch {
                useItem {
                    openFolder(it)
                }
            }
        }
    )

    val deleteAction = simpleAction(
        title = Res.string.delete_category.asStringSource(),
        icon = MyIcons.remove,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestDelete(it)
                }
            }
        },
    )
    val editAction = simpleAction(
        title = Res.string.edit_category.asStringSource(),
        icon = MyIcons.settings,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestEdit(it)
                }
            }
        },
    )

    val addCategoryAction = simpleAction(
        title = Res.string.add_category.asStringSource(),
        icon = MyIcons.add,
        onActionPerformed = {
            scope.launch {
                onRequestAddCategory()
            }
        },
    )
    val categorizeItemsAction = simpleAction(
        title = Res.string.auto_categorize_downloads.asStringSource(),
        icon = MyIcons.refresh,
        onActionPerformed = {
            scope.launch {
                onRequestCategorizeItems()
            }
        },
    )
    val resetToDefaultAction = simpleAction(
        title = Res.string.restore_defaults.asStringSource(),
        icon = MyIcons.undo,
        checkEnable = categoryManager
            .categoriesFlow
            .mapStateFlow { !defaultCategories.isDefault(it) },
        onActionPerformed = {
            scope.launch {
                onRequestResetToDefaults()
            }
        },
    )

    val menu: List<MenuItem> = buildMenu {
        +editAction
        +openCategoryFolderAction
        +deleteAction
        separator()
        +addCategoryAction
        separator()
        +categorizeItemsAction
        +resetToDefaultAction
    }
}

class HomeComponent(
    ctx: ComponentContext,
    private val downloadItemOpener: DownloadItemOpener,
    private val downloadDialogManager: DownloadDialogManager,
    private val editDownloadDialogManager: EditDownloadDialogManager,
    private val addDownloadDialogManager: AddDownloadDialogManager,
    private val fileChecksumDialogManager: FileChecksumDialogManager,
    private val categoryDialogManager: CategoryDialogManager,
    private val notificationSender: NotificationSender,
) : BaseComponent(ctx),
    ContainsShortcuts,
    ContainsEffects<HomeEffects> by supportEffects(),
    KoinComponent {
    private val downloadSystem: DownloadSystem by inject()
    private val queueManager: QueueManager by inject()
    private val pageStorage: PageStatesStorage by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val updateManager: UpdateManager by inject()
    private val appVersionTracker: AppVersionTracker by inject()
    val filterState = FilterState()
    val mergeTopBarWithTitleBar = appSettings.mergeTopBarWithTitleBar

    private val homePageStateToPersist = MutableStateFlow(pageStorage.homePageStorage.value)

    val categoryManager: CategoryManager by inject()
    private val defaultCategories: DefaultCategories by inject()
    val fileIconProvider: FileIconProvider by inject()

    init {
        HomeComponent.homeComponentCreationCount++
    }

    private fun isFirstVisitInThisSession(): Boolean {
        return HomeComponent.homeComponentCreationCount == 1
    }

    init {
        homePageStateToPersist
            .debounce(500)
            .onEach { newValue ->
                pageStorage.homePageStorage.update { newValue }
            }.launchIn(scope)
    }

    private val _windowSize = homePageStateToPersist.mapTwoWayStateFlow(
        map = {
            it.windowSize.let { (x, y) ->
                DpSize(x.dp, y.dp)
            }
        },
        unMap = {
            copy(
                windowSize = it.width.value to it.height.value
            )
        }
    )
    val windowSize = _windowSize.asStateFlow()
    fun setWindowSize(dpSize: DpSize) {
        _windowSize.value = dpSize
    }

    private val _categoriesWidth = homePageStateToPersist.mapTwoWayStateFlow(
        map = {
            it.categoriesWidth.dp.coerceIn(CATEGORIES_SIZE_RANGE)
        },
        unMap = {
            copy(categoriesWidth = it.coerceIn(CATEGORIES_SIZE_RANGE).value)
        }
    )
    val categoriesWidth = _categoriesWidth.asStateFlow()
    fun setCategoriesWidth(updater: (Dp) -> Dp) {
        _categoriesWidth.value = updater(_categoriesWidth.value)
    }


    private fun requestDelete(
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
                HomeEffects.DeleteItems(
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


    fun requestAddNewDownload(
        link: List<DownloadCredentials> = listOf(DownloadCredentials.empty()),
    ) {
        addDownloadDialogManager.openAddDownloadDialog(link)
    }

    private val _selectionList = MutableStateFlow<List<Long>>(emptyList())
    val selectionList = _selectionList.asStateFlow()

    private val mainItem = MutableStateFlow<Long?>(null)


    val menu: List<MenuItem.SubMenu> = buildMenu {
        subMenu(Res.string.file.asStringSource()) {
            +newDownloadAction
            +newDownloadFromClipboardAction
            +batchDownloadAction
            separator()
            +requestExitAction

        }
        subMenu(Res.string.tasks.asStringSource()) {
//            +toggleQueueAction
            +startQueueGroupAction
            +stopQueueGroupAction
            separator()
            +stopAllAction
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
        }
        subMenu(Res.string.tools.asStringSource()) {
            if (AppInfo.isInDebugMode()) {
                +dummyException
                +dummyMessage
                separator()
            }
            +browserIntegrations
            separator()
            +gotoSettingsAction
        }
        subMenu(Res.string.help.asStringSource()) {
            if (updateManager.isUpdateSupported()) {
                +checkForUpdateAction
            }
            +supportActionGroup
            separator()
            +openOpenSourceThirdPartyLibraries
            +openTranslators
            +openAboutAction
        }
    }.filterIsInstance<MenuItem.SubMenu>()


    private val shouldShowOptions = MutableStateFlow(false)
    val downloadOptions = combineStateFlows(
        shouldShowOptions,
        selectionList,
    ) { shouldShowOptions, selectionList ->
        if (!shouldShowOptions) {
            null
        } else {
            MenuItem.SubMenu(
                icon = null,
                title = if (selectionList.size == 1) {
                    (downloadActions.defaultItem.value?.name ?: "")
                        .asStringSource()
                } else {
                    Res.string.n_items_selected
                        .asStringSourceWithARgs(
                            Res.string.n_items_selected_createArgs(
                                count = selectionList.size.toString()
                            )
                        )
                },
                items = downloadActions.menu
            )
        }
    }

    val tableState = TableState(
        cells = listOf(
            DownloadListCells.Check,
            DownloadListCells.Name,
            DownloadListCells.Size,
            DownloadListCells.Status,
            DownloadListCells.Speed,
            DownloadListCells.TimeLeft,
            DownloadListCells.DateAdded,
        ),
        forceVisibleCells = listOf(
            DownloadListCells.Name,
        ),
        initialSortBy = Sort(DownloadListCells.DateAdded, true)
    ).apply {
        homePageStateToPersist.value.downloadListState?.let {
            load(it)
        }
        onPropChange.onEach {
            homePageStateToPersist.update {
                it.copy(downloadListState = save())
            }
        }.launchIn(scope)
    }


    fun onRequestOpenDownloadItemOption(
        mainItem: IDownloadItemState?,
    ) {
        if (mainItem != null && mainItem.id !in selectionList.value) {
            newSelection(listOf(mainItem.id))
        }
        this.mainItem.value = mainItem?.id
        shouldShowOptions.update { true }
    }

    fun onRequestCloseDownloadItemOption() {
        shouldShowOptions.update { false }
        mainItem.value = null
//        if (selectionList.value.size == 1) {
//            //there is no multiselect so clear it
//            _selectionList.update { emptyList() }
//        }
    }


    fun clearSelection() {
        _selectionList.update { emptyList() }
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

    fun onFilterChange(
        statusCategoryFilter: DownloadStatusCategoryFilter,
        typeCategoryFilter: Category?,
    ) {
        this.filterState.statusFilter = statusCategoryFilter
        this.filterState.typeCategoryFilter = typeCategoryFilter
    }

    fun importLinks(links: List<DownloadCredentials>) {
        val size = links.size
        when {
            size <= 0 -> {
                return
            }

            size > 0 -> {
                requestAddNewDownload(links)
            }
        }
    }

    val currentActiveDrops: MutableStateFlow<List<DownloadCredentials>?> = MutableStateFlow(null)


    private fun parseLinks(v: String): List<DownloadCredentials> {
        return DownloadCredentialFromStringExtractor.extract(v)
            .distinctBy { it.link }
    }

    fun onExternalTextDraggedIn(readText: () -> String) {
        val v = readText()
        val parsedLinks = parseLinks(v)
        currentActiveDrops.update { parsedLinks }
    }

    fun onExternalFilesDraggedIn(getFilePaths: () -> List<File>) {
        val filePaths = kotlin.runCatching { getFilePaths() }
            .getOrNull()?.filter { it.length() <= 1024 * 1024 } ?: return
        onExternalTextDraggedIn {
            filePaths
                .firstOrNull()
                ?.readText()
                .orEmpty()
        }
    }

    fun onLinkPasted(txt: String) {
        importLinks(parseLinks(txt))
    }


    fun onDragExit() {
        currentActiveDrops.update { null }
    }

    fun onDropped() {
        currentActiveDrops.value?.let {
            importLinks(it)
        }
    }


    val activeDownloadCountFlow = downloadSystem.downloadMonitor.activeDownloadCount
    val globalSpeedFlow = downloadSystem.downloadMonitor.activeDownloadListFlow.map {
        it.sumOf { it.speed }
    }


    val activeDownloadList = downloadSystem.downloadMonitor.activeDownloadListFlow
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            emptyList()
        )
    val completedList = downloadSystem.downloadMonitor.completedDownloadListFlow
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            emptyList()
        )

    init {
        categoryManager.categoriesFlow.onEach { categories ->
            val currentCategory = filterState.typeCategoryFilter ?: return@onEach
            filterState.typeCategoryFilter = categories.find {
                it.id == currentCategory.id
            }
        }.launchIn(scope)
    }

    val downloadList = merge(
        snapshotFlow { filterState.textToSearch },
        activeDownloadList,
        completedList,
        snapshotFlow { filterState.typeCategoryFilter },
        snapshotFlow { filterState.statusFilter },
    )
        .map {
            (activeDownloadList.value + completedList.value)
                .filter {
                    val statusAccepted = filterState.statusFilter.accept(it)
//                    val typeAccepted = filterState.typeCategoryFilter?.accept(it.name) ?: true
                    val typeAccepted = filterState.typeCategoryFilter?.items?.contains(it.id) ?: true
                    val searchAccepted = it.name.contains(filterState.textToSearch, ignoreCase = true)
                    typeAccepted && statusAccepted && searchAccepted
                }
                // when restart a completed download item there is a duplication in list
                // so make sure to not pass bad data to download list table as it has  item.id as key
                .distinctBy { it.id }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        downloadList.onEach { downloads ->
            _selectionList.value = selectionList.value.filter { previouslySelectedItem ->
                downloads.any { it.id == previouslySelectedItem }
            }
        }.launchIn(scope)
        if (isFirstVisitInThisSession()) {
            // if the app is updated then clean downloaded files
            if (appVersionTracker.isUpgraded()) {
                // clean update files
                scope.launch {
                    // temporary fix:
                    // at the moment we relly on DownloadMonitor for getting the list of downloads by their folder
                    // so wait for the download list to be updated by the download monitor
                    delay(1000)
                    // then clean up the downloaded files
                    updateManager.cleanDownloadedFiles()
                }
                // show user about update
                scope.launch {
                    // let user focus to the app
                    delay(1000)
                    notificationSender.sendNotification(
                        title = Res.string.update_updater.asStringSource(),
                        description = Res.string.update_app_updated_to_version_n.asStringSourceWithARgs(
                            Res.string.update_app_updated_to_version_n_createArgs(
                                version = appVersionTracker.currentVersion.toString()
                            )
                        ),
                        type = NotificationType.Success,
                        tag = "Updater"
                    )
                }
            }
        }
    }

    private val selectionListItems = combineStateFlows(
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

    fun openFolder(id: Long) {
        scope.launch {
            val dItem = downloadSystem.getDownloadItemById(id) ?: return@launch
            if (dItem.status != DownloadStatus.Completed) {
                return@launch
            }
            downloadItemOpener.openDownloadItemFolder(dItem)
        }
    }

    fun bringToFront() {
        sendEffect(HomeEffects.BringToFront)
    }


    private val downloadActions = DownloadActions(
        scope = scope,
        downloadSystem = downloadSystem,
        downloadDialogManager = downloadDialogManager,
        editDownloadDialogManager = editDownloadDialogManager,
        fileChecksumDialogManager = fileChecksumDialogManager,
        selections = selectionListItems,
        mainItem = mainItem,
        queueManager = queueManager,
        categoryManager = categoryManager,
        openFile = this::openFile,
        openFolder = this::openFolder,
        requestDelete = this::requestDelete,
    )
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
                        FileUtils.openFolder(File(it))
                    }
                }
            },
            onRequestAddCategory = {
                categoryDialogManager.openCategoryDialog(-1)
            },
            requestDelete = {
                sendEffect(
                    HomeEffects.DeleteCategory(it)
                )
            },
            requestEdit = {
                categoryDialogManager.openCategoryDialog(it.id)
            },
            onRequestCategorizeItems = {
                sendEffect(HomeEffects.AutoCategorize)
            },
            onRequestResetToDefaults = {
                sendEffect(HomeEffects.ResetCategoriesToDefault)
            }
        )
    }

    fun closeCategoryOptions() {
        categoryActions.value = null
    }

    override val shortcutManager = DesktopShortcutManager().apply {
        "ctrl N" to newDownloadAction
        "ctrl V" to newDownloadFromClipboardAction
        "ctrl C" to downloadActions.copyDownloadLinkAction
        "ctrl alt S" to gotoSettingsAction
        "ctrl W" to requestExitAction
        "DELETE" to downloadActions.deleteAction
        "ctrl O" to downloadActions.openFileAction
        "ctrl F" to downloadActions.openFolderAction
        "ctrl E" to downloadActions.editDownloadAction
        "ctrl P" to downloadActions.pauseAction
        "ctrl R" to downloadActions.resumeAction
        "DELETE" to downloadActions.deleteAction
        "ctrl I" to downloadActions.openDownloadDialogAction
    }
    val showLabels = appSettings.showIconLabels
    val headerActions = buildMenu {
        separator()
        +downloadActions.resumeAction
        +downloadActions.pauseAction
        separator()
        +startQueueGroupAction
        +stopQueueGroupAction
        +openQueuesAction
        separator()
        +stopAllAction
        separator()
        +downloadActions.deleteAction
        separator()
        +gotoSettingsAction
    }

    companion object {
        private var homeComponentCreationCount = 0
        val CATEGORIES_SIZE_RANGE = 0.dp..500.dp
    }
}
