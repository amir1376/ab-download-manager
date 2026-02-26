package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.desktop.*
import com.abdownloadmanager.desktop.actions.*
import com.abdownloadmanager.desktop.pages.home.sections.DownloadListCells
import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.ui.widget.sort.Sort
import com.abdownloadmanager.shared.ui.widget.table.customtable.TableState
import com.abdownloadmanager.desktop.utils.*
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.desktop.pages.category.DesktopCategoryDialogManager
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.action.donate
import com.abdownloadmanager.shared.action.supportActionGroup
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EnterNewURLDialogManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.pages.home.BaseHomeComponent
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.monitor.*
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapTwoWayStateFlow
import com.abdownloadmanager.shared.util.extractors.linkextractor.DownloadCredentialFromStringExtractor
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.AppVersionTracker
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isLinux
import ir.amirab.util.platform.isMac
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.event.KeyEvent
import java.io.File
import kotlin.collections.map
import kotlin.getValue


class HomeComponent(
    ctx: ComponentContext,
    downloadItemOpener: DownloadItemOpener,
    downloadDialogManager: DesktopDownloadDialogManager,
    editDownloadDialogManager: EditDownloadDialogManager,
    override val enterNewURLDialogManager: EnterNewURLDialogManager,
    desktopAddDownloadDialogManager: DesktopAddDownloadDialogManager,
    fileChecksumDialogManager: FileChecksumDialogManager,
    queuePageManager: QueuePageManager,
    categoryDialogManager: DesktopCategoryDialogManager,
    notificationSender: NotificationSender,
    downloadSystem: DownloadSystem,
    categoryManager: CategoryManager,
    queueManager: QueueManager,
    defaultCategories: DefaultCategories,
    fileIconProvider: FileIconProvider,
) : BaseHomeComponent(
    componentContext = ctx,
    downloadItemOpener = downloadItemOpener,
    downloadDialogManager = downloadDialogManager,
    editDownloadDialogManager = editDownloadDialogManager,
    addDownloadDialogManager = desktopAddDownloadDialogManager,
    fileChecksumDialogManager = fileChecksumDialogManager,
    queuePageManager = queuePageManager,
    categoryDialogManager = categoryDialogManager,
    notificationSender = notificationSender,
    downloadSystem = downloadSystem,
    categoryManager = categoryManager,
    queueManager = queueManager,
    defaultCategories = defaultCategories,
    fileIconProvider = fileIconProvider,
),
    ContainsShortcuts,
    KoinComponent {
    private val pageStorage: PageStatesStorage by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val updateManager: UpdateManager by inject()
    private val appVersionTracker: AppVersionTracker by inject()
    val mergeTopBarWithTitleBar = appSettings.mergeTopBarWithTitleBar
    val useNativeMenuBar = appSettings.useNativeMenuBar

    private val homePageStateToPersist = MutableStateFlow(pageStorage.homePageStorage.value)

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

    private val _isMaximized = homePageStateToPersist.mapTwoWayStateFlow(
        map = {
            it.isMaximized
        },
        unMap = {
            copy(isMaximized = it)
        }
    )
    val isMaximized = _isMaximized.asStateFlow()
    fun setIsMaximized(value: Boolean) {
        _isMaximized.value = value
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
                +shutdown
                separator()
            }
            +browserIntegrations
            if (Platform.isLinux()) {
                +createDesktopEntryAction
            }
            separator()
            +perHostSettings
            +gotoSettingsAction
        }
        subMenu(Res.string.help.asStringSource()) {
            +supportActionGroup
            separator()
            +openOpenSourceThirdPartyLibraries
            +openTranslators
            +donate
            separator()
            +checkForUpdateAction
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
        initialSortBy = Sort(DownloadListCells.DateAdded, Sort.DEFAULT_IS_DESCENDING)
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
    }



    fun importLinks(links: List<AddDownloadCredentialsInUiProps>) {
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

    val currentActiveDrops: MutableStateFlow<List<IDownloadCredentials>?> = MutableStateFlow(null)


    private fun parseLinks(v: String): List<IDownloadCredentials> {
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

    fun onDragExit() {
        currentActiveDrops.update { null }
    }

    fun onDropped() {
        currentActiveDrops.value?.let {
            importLinks(it.map {
                AddDownloadCredentialsInUiProps(credentials = it)
            })
        }
    }

    fun openFolder(id: Long) {
        scope.launch {
            downloadItemOpener.openDownloadItemFolder(id)
        }
    }

    fun bringToFront() {
        sendEffect(Effects.BringToFront)
    }

    init {
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

    private val downloadActions = DesktopDownloadActions(
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

    override val shortcutManager = DesktopShortcutManager().apply {
        val isMac = Platform.isMac()
        val metaKey = if (isMac) "meta" else "ctrl"
        if (isMac) {
            KeyEvent.VK_BACK_SPACE to downloadActions.deleteAction
        } else {
            "DELETE" to downloadActions.deleteAction
        }
        "$metaKey N" to newDownloadAction
        "$metaKey V" to newDownloadFromClipboardAction
        "$metaKey C" to downloadActions.copyDownloadLinkAction
        "$metaKey alt S" to gotoSettingsAction
        "$metaKey Q" to requestExitAction
        "$metaKey O" to downloadActions.openFileAction
        "$metaKey F" to downloadActions.openFolderAction
        "$metaKey E" to downloadActions.editDownloadAction
        "$metaKey P" to downloadActions.pauseAction
        "$metaKey R" to downloadActions.resumeAction
        "$metaKey I" to downloadActions.openDownloadDialogAction
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
    sealed interface Effects : BaseHomeComponent.Effects.PlatformEffects {
        data object BringToFront : Effects
    }
}
