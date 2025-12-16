package com.abdownloadmanager.desktop

import com.abdownloadmanager.UpdateManager
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadConfig
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.desktop.pages.addDownload.multiple.DesktopAddMultiDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.single.DesktopAddSingleDownloadComponent
import com.abdownloadmanager.desktop.pages.batchdownload.DesktopBatchDownloadComponent
import com.abdownloadmanager.shared.pages.category.CategoryComponent
import com.abdownloadmanager.desktop.pages.category.DesktopCategoryDialogManager
import com.abdownloadmanager.desktop.pages.editdownload.DesktopEditDownloadComponent
import com.abdownloadmanager.desktop.pages.enterurl.DesktopEnterNewURLComponent
import com.abdownloadmanager.desktop.pages.checksum.DesktopFileChecksumComponent
import com.abdownloadmanager.desktop.pages.home.HomeComponent
import com.abdownloadmanager.desktop.pages.perhostsettings.DesktopPerHostSettingsComponent
import com.abdownloadmanager.desktop.pages.queue.QueuesComponent
import com.abdownloadmanager.desktop.pages.settings.DesktopSettingsComponent
import com.abdownloadmanager.desktop.pages.poweractionalert.PowerActionComponent
import com.abdownloadmanager.desktop.pages.singleDownloadPage.DesktopSingleDownloadComponent
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.DesktopExtraDownloadItemSettings
import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.desktop.ui.widget.MessageDialogModel
import com.abdownloadmanager.shared.ui.widget.MessageDialogType
import com.abdownloadmanager.shared.ui.widget.NotificationModel
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.navigate
import com.arkivanov.decompose.router.slot.*
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.utils.ExceptionUtils
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import com.abdownloadmanager.resources.*
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pagemanager.AboutPageManager
import com.abdownloadmanager.shared.pagemanager.AddDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.BatchDownloadPageManager
import com.abdownloadmanager.shared.pagemanager.DownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EnterNewURLDialogManager
import com.abdownloadmanager.shared.pagemanager.ExitApplicationRequestManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pagemanager.OpenSourceLibrariesPageManager
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.pagemanager.SettingsPageManager
import com.abdownloadmanager.shared.pagemanager.TranslatorsPageManager
import com.abdownloadmanager.shared.pages.updater.UpdateComponent
import com.abdownloadmanager.shared.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.CategorySelectionMode
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.subscribeAsStateFlow
import com.arkivanov.decompose.childContext
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.destination.IncompleteFileUtil
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.monitor.isDownloadActiveFlow
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.combineStringSources
import ir.amirab.util.coroutines.launchWithDeferred
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Toolkit
import kotlin.system.exitProcess

sealed interface AppEffects {
    data class SimpleNotificationNotification(
        val notificationModel: NotificationModel,
    ) : AppEffects
}

class AppComponent(
    ctx: ComponentContext,
) : BaseComponent(ctx),
    DesktopDownloadDialogManager,
    DesktopAddDownloadDialogManager,
    DesktopCategoryDialogManager,
    EditDownloadDialogManager,
    FileChecksumDialogManager,
    QueuePageManager,
    NotificationSender,
    DownloadItemOpener,
    PerHostSettingsPageManager,
    PowerActionManager,
    EnterNewURLDialogManager,
    SettingsPageManager,
    OpenSourceLibrariesPageManager,
    TranslatorsPageManager,
    AboutPageManager,
    BatchDownloadPageManager,
    ExitApplicationRequestManager,
    ContainsEffects<AppEffects> by supportEffects(),
    KoinComponent {
    val applicationScope: CoroutineScope by inject()
    val appRepository: AppRepository by inject()
    val appSettings: AppSettingsStorage by inject()
    val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    private val queueManager: QueueManager by inject()
    private val defaultCategories: DefaultCategories by inject()
    private val integration: Integration by inject()
    private val perHostSettingsManager: PerHostSettingsManager by inject()
    val iconFromUriResolver: IIconResolver by inject()
    val updaterManager: UpdateManager by inject()
    val extraDownloadSettingStorage: ExtraDownloadSettingsStorage<DesktopExtraDownloadItemSettings> by inject()
    val useSystemTray = appSettings.useSystemTray
    fun openHome() {
        scope.launch {
            showHomeSlot.value.child?.instance.let {
                if (it != null) {
                    it.bringToFront()
                } else {
                    showHome.activate(HomePageConfig())
                }
            }
        }
    }

    fun activateHomeIfNotOpen() {
        scope.launch {
            showHomeSlot.value.child?.instance.let {
                if (it == null) {
                    showHome.activate(HomePageConfig())
                }
            }
        }
    }

    fun closeHome() {
        scope.launch {
            showHome.dismiss()
        }
    }

    @Serializable
    class HomePageConfig

    private val showHome = SlotNavigation<HomePageConfig>()
    val showHomeSlot = childSlot(
        showHome,
        serializer = null,
        key = "home",
        childFactory = { _: HomePageConfig, componentContext: ComponentContext ->
            HomeComponent(
                ctx = componentContext,
                downloadItemOpener = this,
                downloadDialogManager = this,
                enterNewURLDialogManager = this,
                desktopAddDownloadDialogManager = this,
                fileChecksumDialogManager = this,
                categoryDialogManager = this,
                notificationSender = this,
                editDownloadDialogManager = this,
                queuePageManager = this,
                categoryManager = categoryManager,
                downloadSystem = downloadSystem,
                queueManager = queueManager,
                defaultCategories = defaultCategories,
                fileIconProvider = fileIconProvider,
            )
        }
    ).subscribeAsStateFlow()

    class QueuePageConfig(
        val selectedQueue: Long? = null
    )

    private val showQueues = SlotNavigation<QueuePageConfig>()
    val showQueuesSlot = childSlot(
        showQueues,
        serializer = null,
        key = "queues",
        childFactory = { config: QueuePageConfig, componentContext: ComponentContext ->
            QueuesComponent(componentContext, this::closeQueues).apply {
                config.selectedQueue?.let {
                    onQueueSelected(it)
                }
            }
        }
    ).subscribeAsStateFlow()

    class BatchDownloadConfig

    private val batchDownload = SlotNavigation<BatchDownloadConfig>()
    val batchDownloadSlot = childSlot(
        batchDownload,
        serializer = null,
        key = "batchDownload",
        childFactory = { _: BatchDownloadConfig, componentContext: ComponentContext ->
            DesktopBatchDownloadComponent(
                ctx = componentContext,
                onClose = this::closeBatchDownload,
                importLinks = {
                    openAddDownloadDialog(
                        it.mapNotNull {
                            downloaderInUiRegistry
                                .bestMatchForThisLink(it)
                                ?.createMinimumCredentials(it)
                                ?.let { credentials ->
                                    AddDownloadCredentialsInUiProps(
                                        credentials = credentials,
                                    )
                                }
                        }

                    )
                }
            )
        }
    ).subscribeAsStateFlow()

    private val editDownload = SlotNavigation<Long>()
    val editDownloadSlot = childSlot(
        editDownload,
        serializer = null,
        key = "editDownload",
        childFactory = { editDownloadConfig: Long, componentContext: ComponentContext ->
            DesktopEditDownloadComponent(
                ctx = componentContext,
                onRequestClose = {
                    closeEditDownloadDialog()
                },
                onEdited = { updater, downloadJobExtraConfig ->
                    scope.launch {
                        downloadSystem.editDownload(
                            id = editDownloadConfig,
                            applyUpdate = updater,
                            downloadJobExtraConfig = downloadJobExtraConfig
                        )
                        closeEditDownloadDialog()
                    }
                },
                downloadId = editDownloadConfig,
                acceptEdit = downloadSystem.downloadMonitor
                    .isDownloadActiveFlow(editDownloadConfig)
                    .mapStateFlow { !it },
                downloadSystem = downloadSystem,
                downloaderInUiRegistry = downloaderInUiRegistry,
                iconProvider = fileIconProvider,
            )
        }
    ).subscribeAsStateFlow()

    override fun openEditDownloadDialog(id: Long) {
        val currentComponent = editDownloadSlot.value.child?.instance
        if (currentComponent != null && currentComponent.downloadId == id) {
            currentComponent.bringToFront()
        } else {
            editDownload.activate(id)
        }
    }

    override fun closeEditDownloadDialog() {
        editDownload.dismiss()
    }

    override fun openSettings() {
        scope.launch {
            showSettingSlot.value.child?.instance.let {
                if (it != null) {
                    it.toFront()
                } else {
                    showSettingWindow.activate(AppSettingPageConfig())
                }

            }
        }
    }

    override fun closeSettings() {
        scope.launch {
            showSettingWindow.dismiss()
        }
    }

    class AppSettingPageConfig

    val showSettingWindow = SlotNavigation<AppSettingPageConfig>()
    val showSettingSlot = childSlot(
        showSettingWindow,
        serializer = null,
        key = "settings",
        childFactory = { configuration: AppSettingPageConfig, componentContext: ComponentContext ->
            DesktopSettingsComponent(
                componentContext,
                this
            )
        }
    ).subscribeAsStateFlow()
    private val pageStatesStorage: PageStatesStorage by inject()

    val downloadSystem: DownloadSystem by inject()
    private val fileIconProvider: FileIconProvider by inject()
    private val addDownloadPageControl = PagesNavigation<AddDownloadConfig>()
    val _openedAddDownloadDialogs = childPages(
        key = "openedAddDownloadDialogs",
        source = addDownloadPageControl,
        serializer = null,
        initialPages = { Pages() },

        pageStatus = { _, _ ->
            ChildNavState.Status.RESUMED
        },
        childFactory = { config, ctx ->
            val component: AddDownloadComponent = when (config) {
                is AddDownloadConfig.SingleAddConfig -> {
                    DesktopAddSingleDownloadComponent(
                        ctx = ctx,
                        onRequestClose = {
                            closeAddDownloadDialog(config.id)
                        },
                        onRequestAddToQueue = { item, queueId, categoryId ->
                            addDownload(
                                item = item,
                                queueId = queueId,
                                categoryId = categoryId,
                            )
                        },
                        categoryDialogManager = this,
                        onRequestDownload = { item, categoryId ->
                            startNewDownload(
                                item = item,
                                categoryId = categoryId,
                            )
                        },
                        openExistingDownload = {
                            openDownloadDialog(it)
                        },
                        downloadItemOpener = this,
                        updateExistingDownloadCredentials = { id, newCredentials, downloadJobExtraConfig ->
                            scope.launch {
                                downloadSystem.downloadManager.updateDownloadItem(
                                    id = id,
                                    downloadJobExtraConfig = downloadJobExtraConfig,
                                    updater = {
                                        it.withCredentials(newCredentials)
                                    }
                                )
                                openDownloadDialog(id)
                            }
                        },

                        id = config.id,
                        importOptions = config.importOptions,
                        initialCredentials = config.newDownload,
                        downloaderInUi = requireNotNull(
                            downloaderInUiRegistry.getDownloaderOf(config.newDownload.credentials)
                        ),
                        lastSavedLocationsStorage = pageStatesStorage,
                        appScope = applicationScope,
                        appSettings = appSettings,
                        appRepository = appRepository,
                        perHostSettingsManager = perHostSettingsManager,
                        downloadSystem = downloadSystem,
                        iconProvider = fileIconProvider,
                        categoryManager = categoryManager,
                        queueManager = queueManager,
                    )
                }

                is AddDownloadConfig.MultipleAddConfig -> {
                    DesktopAddMultiDownloadComponent(
                        ctx = ctx,
                        id = config.id,
                        onRequestClose = { closeAddDownloadDialog(config.id) },
                        onRequestAdd = { items, queueId, categorySelectionMode ->
                            addDownloads(
                                items = items,
                                queueId = queueId,
                                categorySelectionMode = categorySelectionMode
                            )
                        },
                        lastSavedLocationsStorage = pageStatesStorage,
                        perHostSettingsManager = perHostSettingsManager,
                        downloadSystem = downloadSystem,
                        fileIconProvider = fileIconProvider,
                        appRepository = appRepository,
                        downloaderInUiRegistry = downloaderInUiRegistry,
                        queueManager = queueManager,
                        categoryManager = categoryManager,
                        categoryDialogManager = this,
                    ).apply { addItems(config.newDownloads) }
                }

                else -> error("should not happened")
            }
            component
        }
    ).subscribeAsStateFlow()
    override val openedAddDownloadDialogs = _openedAddDownloadDialogs.map {
        it.items.mapNotNull { it.instance }
    }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val downloadDialogControl = PagesNavigation<DesktopSingleDownloadComponent.Config>()

    private val _openedDownloadDialogs = childPages(
        key = "openedDownloadDialogs",
        source = downloadDialogControl,
        serializer = null,
        initialPages = { Pages() },
        pageStatus = { _, _ ->
            ChildNavState.Status.RESUMED
        },
        childFactory = { cfg, ctx ->
            DesktopSingleDownloadComponent(
                ctx = ctx,
                downloadItemOpener = this,
                onDismiss = {
                    closeDownloadDialog(listOf(cfg.id))
                },
                downloadId = cfg.id,
                downloadSystem = downloadSystem,
                appSettings = appSettings,
                appRepository = appRepository,
                applicationScope = applicationScope,
                fileIconProvider = fileIconProvider,
                extraDownloadSettingsStorage = extraDownloadSettingStorage,
            )
        }
    ).subscribeAsStateFlow()

    override val openedDownloadDialogs = _openedDownloadDialogs
        .map { it.items.mapNotNull { it.instance } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val categoryManager: CategoryManager by inject()

    private val categoryPageControl = PagesNavigation<Long>()
    private val _openedCategoryDialogs = childPages(
        key = "openedCategoryDialogs",
        source = categoryPageControl,
        serializer = null,
        initialPages = { Pages() },
        pageStatus = { _, _ ->
            ChildNavState.Status.RESUMED
        },
        childFactory = { cfg, ctx ->
            CategoryComponent(
                ctx = ctx,
                close = {
                    closeCategoryDialog(cfg)
                },
                submit = { submittedCategory ->
                    if (submittedCategory.id < 0) {
                        categoryManager.addCustomCategory(submittedCategory)
                    } else {
                        categoryManager.updateCategory(
                            submittedCategory.id
                        ) {
                            submittedCategory.copy(
                                items = it.items
                            )
                        }
                    }
                    closeCategoryDialog(cfg)
                },
                id = cfg
            )
        }
    ).subscribeAsStateFlow()
    override val openedCategoryDialogs: StateFlow<List<CategoryComponent>> = _openedCategoryDialogs
        .map {
            it.items.mapNotNull { it.instance }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun openCategoryDialog(categoryId: Long) {
        scope.launch {
            val component = openedCategoryDialogs.value.find {
                it.id == categoryId
            }
            if (component != null) {
//                component.bringToFront()
            } else {
                categoryPageControl.navigate {
                    val newItems = (it.items.toSet() + categoryId).toList()
                    val copy = it.copy(
                        items = newItems,
                        selectedIndex = newItems.lastIndex
                    )
                    copy
                }
            }
        }
    }

    override fun closeCategoryDialog(categoryId: Long) {
        scope.launch {
            categoryPageControl.navigate {
                val newItems = it.items.filter { config ->
                    config != categoryId
                }
                it.copy(items = newItems, selectedIndex = newItems.lastIndex)
            }
        }
    }
    override fun closeCategoryDialog() {
        scope.launch {
            categoryPageControl.navigate {
                Pages()
            }
        }
    }

    init {
        downloadSystem.downloadEvents
            .filterIsInstance<DownloadManagerEvents.OnJobRemoved>()
            .onEach {
                closeDownloadDialog(listOf(it.downloadItem.id))
            }.launchIn(scope)
    }

    override fun sendNotification(tag: Any, title: StringSource, description: StringSource, type: NotificationType) {
        beep()
        showNotification(tag = tag, title = title, description = description, type = type)
    }

    override fun sendDialogNotification(
        title: StringSource,
        description: StringSource,
        type: MessageDialogType,
    ) {
        beep()
        newDialogMessage(MessageDialogModel(title = title, description = description, type = type))
    }

    private fun beep() {
        if (appSettings.notificationSound.value) {
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun showNotification(
        tag: Any,
        title: StringSource,
        description: StringSource,
        type: NotificationType = NotificationType.Info,
    ) {
        sendEffect(
            AppEffects.SimpleNotificationNotification(
                NotificationModel(
                    tag = tag,
                    initialTitle = title,
                    initialDescription = description,
                    initialNotificationType = type
                )
            )
        )
    }

    init {
        downloadSystem
            .downloadEvents
            .onEach {
                onNewDownloadEvent(it)
            }
            .launchIn(scope)
//        IntegrationPortBroadcaster.cleanOnClose()
        integration
            .integrationStatus
            .onEach {
                when (it) {
                    is IntegrationResult.Fail -> {
                        IntegrationPortBroadcaster.setIntegrationPortInFile(null)
                        sendDialogNotification(
                            title = Res.string.cant_run_browser_integration.asStringSource(),
                            type = MessageDialogType.Error,
                            description = it.throwable.localizedMessage.asStringSource()
                        )
                    }

                    IntegrationResult.Inactive -> {
                        IntegrationPortBroadcaster.setIntegrationPortInFile(null)
                    }

                    is IntegrationResult.Success -> {
                        IntegrationPortBroadcaster.setIntegrationPortInFile(it.port)
                    }
                }
            }.launchIn(scope)
    }

    private fun onNewDownloadEvent(it: DownloadManagerEvents) {
        if (it.context[ResumedBy]?.by !is User) {
            //only notify events that is started by user
            return
        }
//                or
//                val qm = downloadSystem.queueManager
//                val queueId = qm.findItemInQueue(it.downloadItem.id)
//                if (queueId != null) {
//                    return@onEach
//                    // skip download events when download is triggered by queue
////                    if (qm.getQueue(queue).isQueueActive){
////                      return@onEach
////                    }
//                }
        if (it is DownloadManagerEvents.OnJobCanceled) {
            val exception = it.e
            if (ExceptionUtils.isNormalCancellation(exception)) {
                return
            }
            var isMaxTryReachedError = false
            val actualCause = if (exception is TooManyErrorException) {
                isMaxTryReachedError = true
                exception.findActualDownloadErrorCause()
            } else exception
            if (ExceptionUtils.isNormalCancellation(actualCause)) {
                return
            }
            val prefix = if (isMaxTryReachedError) {
                "Too Many Error: "
            } else {
                "Error: "
            }.asStringSource()
            val reason = actualCause.message?.asStringSource() ?: Res.string.unknown.asStringSource()
            sendNotification(
                "downloadId=${it.downloadItem.id}",
                title = it.downloadItem.name.asStringSource(),
                description = listOf(prefix, reason).combineStringSources(),
                type = NotificationType.Error,
            )
        }
        if (it is DownloadManagerEvents.OnJobCompleted) {
            sendNotification(
                tag = "downloadId=${it.downloadItem.id}",
                title = it.downloadItem.name.asStringSource(),
                description = Res.string.finished.asStringSource(),
                type = NotificationType.Success,
            )
            if (appSettings.showDownloadCompletionDialog.value) {
                openDownloadDialog(it.downloadItem.id)
            }
        }
        if (it is DownloadManagerEvents.OnJobStarting) {
            if (appSettings.showDownloadProgressDialog.value) {
                openDownloadDialog(it.downloadItem.id)
            }
        }
    }

    override suspend fun openDownloadItem(id: Long) {
        val item = downloadSystem.getDownloadItemById(id)
        if (item == null) {
            sendNotification(
                Res.string.open_file,
                Res.string.cant_open_file.asStringSource(),
                Res.string.download_item_not_found.asStringSource(),
                NotificationType.Error,
            )
            return
        }
        openDownloadItem(item)
    }

    override suspend fun openDownloadItem(downloadItem: IDownloadItem) {
        runCatching {
            withContext(Dispatchers.IO) {
                FileUtils.openFile(downloadSystem.getDownloadFile(downloadItem))
            }
        }.onFailure {
            sendNotification(
                Res.string.open_file,
                Res.string.cant_open_file.asStringSource(),
                it.localizedMessage?.asStringSource() ?: Res.string.unknown_error.asStringSource(),
                NotificationType.Error,
            )
            println("Can't open file:${it.message}")
        }
    }

    override suspend fun openDownloadItemFolder(id: Long) {
        val item = downloadSystem.getDownloadItemById(id)
        if (item == null) {
            sendNotification(
                Res.string.open_folder,
                Res.string.cant_open_folder.asStringSource(),
                Res.string.download_item_not_found.asStringSource(),
                NotificationType.Error,
            )
            return
        }
        openDownloadItemFolder(item)
    }

    override suspend fun openDownloadItemFolder(downloadItem: IDownloadItem) {
        runCatching {
            withContext(Dispatchers.IO) {
                val file = downloadSystem.getDownloadFile(downloadItem)
                if (file.exists()) {
                    FileUtils.openFolderOfFile(file)
                } else {
                    val incompleteFile = IncompleteFileUtil.addIncompleteIndicator(file, downloadItem.id)
                    if (incompleteFile.exists() && downloadItem.status != DownloadStatus.Completed) {
                        FileUtils.openFolderOfFile(incompleteFile)
                    } else {
                        FileUtils.openFolder(file.parentFile)
                    }
                }
            }
        }.onFailure {
            sendNotification(
                Res.string.open_folder,
                Res.string.cant_open_folder.asStringSource(),
                it.localizedMessage?.asStringSource() ?: Res.string.unknown_error.asStringSource(),
                NotificationType.Error,
            )
            println("Can't open folder:${it.message}")
        }
    }

    fun externalCredentialComingIntoApp(
        list: List<AddDownloadCredentialsInUiProps>,
        options: ImportOptions
    ) {
        val editDownloadComponent = editDownloadSlot.value.child?.instance
        if (editDownloadComponent != null) {
            list.firstOrNull()?.let {
                editDownloadComponent.importCredential(
                    it.credentials
                )
                editDownloadComponent.bringToFront()
            }
        } else {
            openAddDownloadDialog(list, options)
        }
    }

    override fun openAddDownloadDialog(
        links: List<AddDownloadCredentialsInUiProps>,
        importOptions: ImportOptions,
    ) {
        scope.launch {
            //remove duplicates
            val addDownloadCredentialsProps = links.distinctBy {
                it.credentials
            }
            addDownloadPageControl.navigate {
                val newItems = buildList {
                    addAll(it.items)
                    if (addDownloadCredentialsProps.size > 1) {
                        add(
                            AddDownloadConfig.MultipleAddConfig(
                                addDownloadCredentialsProps,
                                importOptions,
                            )
                        )
                    } else {
                        add(
                            AddDownloadConfig.SingleAddConfig(
                                addDownloadCredentialsProps.first(),
                                importOptions,
                            )
                        )
                    }
                }
                val copy = it.copy(
                    items = newItems,
                    selectedIndex = newItems.lastIndex
                )
                copy
            }
        }
    }

    override fun closeAddDownloadDialog(dialogId: String) {
        scope.launch {
            addDownloadPageControl.navigate {
                val newItems = it.items.filter { config ->
                    config.id != dialogId
                }
                it.copy(items = newItems, selectedIndex = newItems.lastIndex)
            }
        }
    }
    override fun closeAddDownloadDialog() {
        scope.launch {
            addDownloadPageControl.navigate {
                Pages()
            }
        }
    }

    override fun openDownloadDialog(id: Long) {
        scope.launch {
            val component = openedDownloadDialogs.value.find {
                it.downloadId == id
            }
            if (component != null) {
                component.bringToFront()
            } else {
                downloadDialogControl.navigate {
                    val newItems = (it.items.toSet() + DesktopSingleDownloadComponent.Config(id)).toList()
                    val copy = it.copy(
                        items = newItems,
                        selectedIndex = newItems.lastIndex
                    )
                    copy
                }
            }

        }
    }

    override fun closeDownloadDialog(ids: List<Long>) {
        scope.launch {
            downloadDialogControl.navigate {
                val newItems = it.items.filter { config ->
                    config.id !in ids
                }
                it.copy(items = newItems, selectedIndex = newItems.lastIndex)
            }
        }
    }

    override fun closeDownloadDialog() {
        scope.launch {
            downloadDialogControl.navigate {
                Pages()
            }
        }
    }

    private val fileChecksumPagesControl = SlotNavigation<DesktopFileChecksumComponent.Config>()
    val openedFileChecksumDialog = childSlot(
        key = "openedFileChecksumPage",
        source = fileChecksumPagesControl,
        serializer = null,
        childFactory = { config, ctx ->
            DesktopFileChecksumComponent(
                ctx = ctx,
                id = config.id,
                itemIds = config.itemIds,
                closeComponent = {
                    closeFileChecksumPage(config.id)
                },
                downloadSystem = downloadSystem,
            )
        }
    ).subscribeAsStateFlow()

    override fun openFileChecksumPage(ids: List<Long>) {
        scope.launch {
            val instance = openedFileChecksumDialog.value.child?.instance
            if (instance?.itemIds == ids) {
                instance.bringToFront()
            } else {
                fileChecksumPagesControl.navigate {
                    DesktopFileChecksumComponent.Config(itemIds = ids)
                }
            }
        }
    }

    override fun closeFileChecksumPage(dialogId: String) {
        scope.launch {
            fileChecksumPagesControl.dismiss()
        }
    }

    fun addDownloads(
        items: List<NewDownloadItemProps>,
        categorySelectionMode: CategorySelectionMode?,
        queueId: Long?,
    ): Deferred<List<Long>> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newItemsToAdd = items,
                queueId = queueId,
                categorySelectionMode = categorySelectionMode,
            )
        }
    }

    fun addDownload(
        item: NewDownloadItemProps,
        queueId: Long?,
        categoryId: Long?,
    ): Deferred<Long> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newDownload = item,
                queueId = queueId,
                categoryId = categoryId,
            )
        }
    }

    fun startNewDownload(
        item: NewDownloadItemProps,
        categoryId: Long?,
    ): Deferred<Long> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newDownload = item,
                queueId = DefaultQueueInfo.ID,
                categoryId = categoryId,
            ).also {
                downloadSystem.manualResume(it)
            }
        }
    }

    private val _showConfirmExitDialog = MutableStateFlow(false)
    val showConfirmExitDialog = _showConfirmExitDialog.asStateFlow()

    fun exitAppAsync() {
        scope.launch { exitApp() }
    }

    suspend fun exitApp() {
        downloadSystem.stopAnything()
        exitProcess(0)
    }

    fun closeConfirmExit() {
        _showConfirmExitDialog.value = false
    }

    override suspend fun requestExitApp() {
        val hasActiveDownloads = downloadSystem.downloadMonitor.activeDownloadCount.value > 0
        if (hasActiveDownloads) {
            _showConfirmExitDialog.value = true
            return
        }
        exitApp()
    }

    override fun openAboutPage() {
        showAboutPage.update { true }
    }

    fun closeAbout() {
        showAboutPage.update { false }
    }

    override fun openOpenSourceLibrariesPage() {
        showOpenSourceLibraries.update { true }
    }

    fun closeOpenSourceLibraries() {
        showOpenSourceLibraries.update { false }
    }

    override fun openTranslatorsPage() {
        showTranslators.update { true }
    }

    override fun closeTranslatorsPage() {
        showTranslators.update { false }
    }

    override fun openQueues(
        openQueueId: Long?,
    ) {
        scope.launch {
            showQueuesSlot.value.child?.instance.let {
                if (it != null) {
                    it.bringToFront()
                    if (openQueueId != null) {
                        it.onQueueSelected(openQueueId)
                    }
                } else {
                    showQueues.activate(
                        QueuePageConfig(
                            selectedQueue = openQueueId
                        )
                    )
                }
            }
        }
    }

    override fun closeQueues() {
        showQueues.dismiss()
    }

    var showCreateQueueDialog = MutableStateFlow(false)
        private set

    override fun closeNewQueueDialog() {
        showCreateQueueDialog.update { false }
    }

    override fun openNewQueueDialog() {
        showCreateQueueDialog.update { true }
    }

    fun createNewQueue(name: String) {
        scope.launch {
            downloadSystem.addQueue(name)
        }
    }

    override fun openBatchDownloadPage() {
        scope.launch {

            batchDownloadSlot.value.child?.instance.let {
                if (it != null) {
                    it.bringToFront()
                } else {
                    batchDownload.activate(BatchDownloadConfig())
                }
            }
        }
    }

    override fun closeBatchDownload() {
        batchDownload.dismiss()
    }

    val enterNewURLWindow = SlotNavigation<DesktopEnterNewURLComponent.Config>()
    val enterNewURLWindowSlot = childSlot(
        enterNewURLWindow,
        serializer = null,
        key = "enterNewURLWindow",
        childFactory = { configuration: DesktopEnterNewURLComponent.Config, componentContext: ComponentContext ->
            DesktopEnterNewURLComponent(
                ctx = componentContext,
                config = configuration,
                downloaderInUiRegistry = downloaderInUiRegistry,
                onCloseRequest = {
                    closeEnterNewURLWindow()
                },
                onRequestFinished = { credentials ->
                    scope.launch {
                        openAddDownloadDialog(
                            links = listOf(
                                AddDownloadCredentialsInUiProps(
                                    credentials = credentials
                                )
                            ),
                        )
                    }
                }
            )
        }
    ).subscribeAsStateFlow()

    override fun openEnterNewURLWindow() {
        scope.launch {
            enterNewURLWindowSlot.value.child?.instance.let {
                if (it != null) {
                    it.bringToFront()
                } else {
                    enterNewURLWindow.activate(
                        DesktopEnterNewURLComponent.Config
                    )
                }
            }
        }
    }

    override fun closeEnterNewURLWindow() {
        scope.launch {
            enterNewURLWindow.dismiss()
        }
    }

    val dialogMessages: MutableStateFlow<List<MessageDialogModel>> = MutableStateFlow(emptyList())
    private fun newDialogMessage(msgDialogModel: MessageDialogModel) {
        dialogMessages.update {
            it
                .filter { item -> item.id != msgDialogModel.id }
                .plus(msgDialogModel)
        }
    }

    fun onDismissDialogMessage(msgDialogModel: MessageDialogModel) {
        dialogMessages.update {
            it.filter { item ->
                msgDialogModel.id != item.id
            }
        }
    }

    fun isReady(): Boolean {
        return listOf(
            IntegrationPortBroadcaster.isInitialized(),
        ).all { it }
    }

    val powerActionNavigation = SlotNavigation<PowerActionComponent.Config>()
    val openedPowerAction = childSlot(
        source = powerActionNavigation,
        key = "powerAction",
        serializer = null,
        childFactory = { config, ctx ->
            PowerActionComponent(
                ctx = ctx,
                powerActionConfig = config.powerActionConfig,
                powerActionDelay = config.powerActionDelay,
                powerActionReason = config.powerActionReason,
                close = ::dismissPowerAction,
                onBeforePowerAction = {
                    downloadSystem.stopAnything()
                },
            )
        }
    ).subscribeAsStateFlow()

    override fun initiatePowerAction(
        powerActionConfig: PowerActionConfig,
        reason: PowerActionComponent.PowerActionReason,
    ) {
        scope.launch {
            powerActionNavigation.activate(
                PowerActionComponent.Config(
                    powerActionConfig = powerActionConfig,
                    powerActionReason = reason,
                )
            )
        }
    }

    override fun dismissPowerAction() {
        scope.launch {
            powerActionNavigation.dismiss()
        }
    }

    val updater = UpdateComponent(
        childContext("updater"),
        this,
        updaterManager,
    )


    private val perHostSettings = SlotNavigation<DesktopPerHostSettingsComponent.Config>()
    val perHostSettingsSlot = childSlot(
        perHostSettings,
        serializer = null,
        key = "perHostSettings",
        childFactory = { cfg: DesktopPerHostSettingsComponent.Config, componentContext: ComponentContext ->
            DesktopPerHostSettingsComponent(
                ctx = componentContext,
                closeRequested = this::closePerHostSettings,
                appScope = applicationScope,
                perHostSettingsManager = perHostSettingsManager,
                appRepository = appRepository,
            ).apply {
                cfg.openedHost?.let(this::onHostSelected)
            }
        }
    ).subscribeAsStateFlow()

    override fun openPerHostSettings(
        openedHost: String?
    ) {
        scope.launch {
            perHostSettingsSlot.value.child?.instance.let { component ->
                if (component != null) {
                    component.bringToFront()
                    openedHost?.let {
                        component.onHostSelected(it)
                    }
                } else {
                    perHostSettings.activate(DesktopPerHostSettingsComponent.Config(openedHost))
                }
            }
        }
    }

    override fun closePerHostSettings() {
        perHostSettings.dismiss { }
    }


    val showAboutPage = MutableStateFlow(false)
    val showOpenSourceLibraries = MutableStateFlow(false)
    val showTranslators = MutableStateFlow(false)
    val theme = appRepository.theme
    val uiScale = appRepository.uiScale
}

interface DesktopDownloadDialogManager : DownloadDialogManager {
    val openedDownloadDialogs: StateFlow<List<DesktopSingleDownloadComponent>>
    fun closeDownloadDialog(ids: List<Long>)
}

interface DesktopAddDownloadDialogManager : AddDownloadDialogManager {
    val openedAddDownloadDialogs: StateFlow<List<AddDownloadComponent>>
    fun closeAddDownloadDialog(dialogId: String)
}

interface PowerActionManager {
    fun initiatePowerAction(
        powerActionConfig: PowerActionConfig,
        reason: PowerActionComponent.PowerActionReason,
    )

    fun dismissPowerAction()
}

