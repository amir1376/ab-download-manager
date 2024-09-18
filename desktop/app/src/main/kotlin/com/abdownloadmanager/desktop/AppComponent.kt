package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadConfig
import com.abdownloadmanager.desktop.pages.addDownload.multiple.AddMultiDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.single.AddSingleDownloadComponent
import com.abdownloadmanager.desktop.pages.home.HomeComponent
import com.abdownloadmanager.desktop.pages.queue.QueuesComponent
import com.abdownloadmanager.desktop.pages.settings.SettingsComponent
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadComponent
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.ui.widget.MessageDialogModel
import com.abdownloadmanager.desktop.ui.widget.MessageDialogType
import com.abdownloadmanager.desktop.ui.widget.NotificationModel
import com.abdownloadmanager.desktop.ui.widget.NotificationType
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.navigate
import com.arkivanov.decompose.router.slot.*
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.utils.OnDuplicateStrategy
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.integration.IntegrationResult
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Toolkit
import kotlin.system.exitProcess

sealed interface AppEffects {
    data class SimpleNotificationNotification(
        val notificationModel: NotificationModel
    ) : AppEffects
}

interface NotificationSender{
    fun sendDialogNotification(title: String,description: String,type: MessageDialogType)
    fun sendNotification(tag: Any,title:String,description: String,type: NotificationType)
}

class AppComponent(
    ctx: ComponentContext,
) : BaseComponent(ctx),
    DownloadDialogManager,
    AddDownloadDialogManager,
    NotificationSender,
    DownloadItemOpener,
    ContainsEffects<AppEffects> by supportEffects(),
    KoinComponent {
    private val appRepository: AppRepository by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val integration: Integration by inject()

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
                addDownloadDialogManager = this,
                notificationSender = this
            )
        }
    ).subscribeAsStateFlow()

    class QueuePageConfig

    private val showQueues = SlotNavigation<QueuePageConfig>()
    val showQueuesSlot = childSlot(
        showQueues,
        serializer = null,
        key = "queues",
        childFactory = { _: QueuePageConfig, componentContext: ComponentContext ->
            QueuesComponent(componentContext, this::closeQueues)
        }
    ).subscribeAsStateFlow()


    fun openSettings() {
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

    fun closeSettings() {
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
            SettingsComponent(componentContext)
        }
    ).subscribeAsStateFlow()


    val downloadSystem: DownloadSystem by inject()
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
                    AddSingleDownloadComponent(
                        ctx = ctx,
                        onRequestClose = {
                            closeAddDownloadDialog(config.id)
                        },
                        onRequestAddToQueue = { item, queueId, onDuplicate ->
                            addDownload(item = item, queueId = queueId, onDuplicateStrategy = onDuplicate)
                            closeAddDownloadDialog(dialogId = config.id)
                        },
                        onRequestDownload = { item, onDuplicate ->
                            startNewDownload(item, onDuplicate, true)
                            closeAddDownloadDialog(config.id)
                        },
                        openExistingDownload = {
                            openDownloadDialog(it)
                            closeAddDownloadDialog(config.id)
                        },
                        downloadItemOpener = this,
                        id = config.id
                    ).also {
                        it.setCredentials(config.credentials)
                    }
                }

                is AddDownloadConfig.MultipleAddConfig -> {
                    AddMultiDownloadComponent(
                        ctx,
                        config.id,
                        { closeAddDownloadDialog(config.id) },
                        { items, strategy, queueId ->
                            addDownload(
                                items = items,
                                onDuplicateStrategy = strategy,
                                queueId = queueId,
                            )
                            closeAddDownloadDialog(config.id)
                        }
                    ).apply { addItems(config.links) }
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

    private val downloadDialogControl = PagesNavigation<SingleDownloadComponent.Config>()

    private val _openedDownloadDialogs = childPages(
        key = "openedDownloadDialogs",
        source = downloadDialogControl,
        serializer = null,
        initialPages = { Pages() },
        pageStatus = { _, _ ->
            ChildNavState.Status.RESUMED
        },
        childFactory = { cfg, ctx ->
            SingleDownloadComponent(
                ctx = ctx,
                downloadItemOpener = this,
                onDismiss = {
                    closeDownloadDialog(cfg.id)
                },
                downloadId = cfg.id,
            )
        }
    ).subscribeAsStateFlow()

    override val openedDownloadDialogs = _openedDownloadDialogs
        .map { it.items.mapNotNull { it.instance } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        downloadSystem.downloadEvents
            .filterIsInstance<DownloadManagerEvents.OnJobRemoved>()
            .onEach {
                closeDownloadDialog(it.downloadItem.id)
            }.launchIn(scope)
    }

    override fun sendNotification(tag: Any, title: String, description: String, type: NotificationType) {
        beep()
        showNotification(tag = tag, title = title, description = description, type = type)
    }

    override fun sendDialogNotification(
        title: String,
        description: String,
        type: MessageDialogType,
    ) {
        beep()
        newDialogMessage(MessageDialogModel(title = title, description = description, type = type,))
    }

    private fun beep() {
        if (appSettings.notificationSound.value){
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun showNotification(
        tag: Any,
        title: String,
        description: String,
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
                            title = "Can't run browser integration",
                            type = MessageDialogType.Error,
                            description = it.throwable.localizedMessage
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
        if (it.context[ResumedBy]?.by !is User){
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
            val actualCause = if (exception is TooManyErrorException){
                isMaxTryReachedError=true
                exception.findActualDownloadErrorCause()
            }else exception
            if (ExceptionUtils.isNormalCancellation(actualCause)) {
                return
            }
            val prefix = if (isMaxTryReachedError) {
                "Too Many Error: "
            }else{
                "Error: "
            }
            val reason = actualCause.message?:"Unknown"
            sendNotification(
                "downloadId=${it.downloadItem.id}",
                title = it.downloadItem.name,
                description = prefix + reason,
                type = NotificationType.Error,
            )
        }
        if (it is DownloadManagerEvents.OnJobCompleted) {
            sendNotification(
                tag = "downloadId=${it.downloadItem.id}",
                title = it.downloadItem.name,
                description = "Finished",
                type = NotificationType.Success,
            )
        }
    }

    override suspend fun openDownloadItem(id: Long) {
        val item = downloadSystem.getDownloadItemById(id)
        if (item==null){
            sendNotification(
                "Open File",
                "Can't open file",
                "Download Item not found",
                NotificationType.Error,
            )
            return
        }
        openDownloadItem(item)
    }
    override fun openDownloadItem(downloadItem: DownloadItem) {
        runCatching {
            FileUtils.openFile(downloadSystem.getDownloadFile(downloadItem))
        }.onFailure {
            sendNotification(
                "Open File",
                "Can't open file",
                it.localizedMessage ?: "Unknown Error",
                NotificationType.Error,
            )
            println("Can't open file:${it.message}")
        }
    }

    override suspend fun openDownloadItemFolder(id: Long) {
        val item = downloadSystem.getDownloadItemById(id)
        if (item==null){
            sendNotification(
                "Open Folder",
                "Can't open folder",
                "Download Item not found",
                NotificationType.Error,
            )
            return
        }
        openDownloadItemFolder(item)
    }

    override fun openDownloadItemFolder(downloadItem: DownloadItem) {
        runCatching {
            FileUtils.openFolderOfFile(downloadSystem.getDownloadFile(downloadItem))
        }.onFailure {
            sendNotification(
                "Open Folder",
                "Can't open folder",
                it.localizedMessage ?: "Unknown Error",
                NotificationType.Error,
            )
            println("Can't open folder:${it.message}")
        }
    }

    override fun openAddDownloadDialog(
        links: List<DownloadCredentials>
    ) {
        scope.launch {
            //remove duplicates
            val links = links.distinct()
            addDownloadPageControl.navigate {
                val newItems = it.items +
                        if (links.size > 1) {
                            AddDownloadConfig.MultipleAddConfig(links)
                        } else {
                            AddDownloadConfig.SingleAddConfig(
                                links.firstOrNull() ?: DownloadCredentials.empty()
                            )
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

    override fun openDownloadDialog(id: Long) {
        scope.launch {
            val component = openedDownloadDialogs.value.find {
                it.downloadId == id
            }
            if (component != null) {
                component.bringToFront()
            } else {
                downloadDialogControl.navigate {
                    val newItems = (it.items.toSet() + SingleDownloadComponent.Config(id)).toList()
                    val copy = it.copy(
                        items = newItems,
                        selectedIndex = newItems.lastIndex
                    )
                    copy
                }
            }

        }
    }

    override fun closeDownloadDialog(id: Long) {
        scope.launch {
            downloadDialogControl.navigate {
                val newItems = it.items.filter { config ->
                    config.id != id
                }
                it.copy(items = newItems, selectedIndex = newItems.lastIndex)
            }
        }
    }

    fun addDownload(
        items: List<DownloadItem>,
        onDuplicateStrategy: (DownloadItem) -> OnDuplicateStrategy,
        queueId: Long?,
    ) {
        scope.launch {
            downloadSystem.addDownload(
                newItemsToAdd = items,
                onDuplicateStrategy = onDuplicateStrategy,
                queueId = queueId,
            )
        }
    }

    fun addDownload(
        item: DownloadItem,
        queueId: Long?,
        onDuplicateStrategy: OnDuplicateStrategy,
    ) {
        scope.launch {
            downloadSystem.addDownload(
                downloadItem = item,
                onDuplicateStrategy = onDuplicateStrategy,
                queueId = queueId,
            )
        }
    }

    fun startNewDownload(
        item: DownloadItem,
        onDuplicateStrategy: OnDuplicateStrategy,
        openDownloadDialog: Boolean,
    ) {
        scope.launch {
            val id = downloadSystem.addDownload(
                item,
                onDuplicateStrategy,
                DefaultQueueInfo.ID,
            )
            launch {
                downloadSystem.manualResume(id)
            }
            if (openDownloadDialog) {
                launch {
                    openDownloadDialog(id)
                }
            }
        }
    }

    fun requestClose() {
        exitProcess(0)
    }

    fun openAbout() {
        showAboutPage.update { true }
    }

    fun closeAbout() {
        showAboutPage .update { false }
    }

    fun openOpenSourceLibraries() {
        showOpenSourceLibraries .update { true }
    }
    fun closeOpenSourceLibraries() {
        showOpenSourceLibraries .update { false }
    }

    fun openQueues() {
        scope.launch {
            showQueuesSlot.value.child?.instance.let {
                if (it!=null){
                    it.bringToFront()
                }else{
                    showQueues.activate(QueuePageConfig())
                }
            }
        }
    }

    fun closeQueues() {
        showQueues.dismiss()
    }

    var showCreateQueueDialog = MutableStateFlow(false)
        private set

    fun closeNewQueueDialog() {
        showCreateQueueDialog.update { false }
    }

    fun openNewQueueDialog() {
        showCreateQueueDialog.update { true }
    }

    fun createNewQueue(name: String) {
        scope.launch {
            downloadSystem.addQueue(name)
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
//    TODO enable updater
//    val updater = UpdateComponent(childContext("updater"))
    val showAboutPage=MutableStateFlow(false)
    val showOpenSourceLibraries=MutableStateFlow(false)
    val theme = appRepository.theme
//    val uiScale = appRepository.uiScale
}

interface DownloadDialogManager {
    val openedDownloadDialogs: StateFlow<List<SingleDownloadComponent>>
    fun openDownloadDialog(id: Long)
    fun closeDownloadDialog(id: Long)
}

interface AddDownloadDialogManager {
    val openedAddDownloadDialogs: StateFlow<List<AddDownloadComponent>>
    fun openAddDownloadDialog(
        links: List<DownloadCredentials>,
    )

    fun closeAddDownloadDialog(dialogId: String)
}