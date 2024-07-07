package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.DownloadUiChecker
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.withCredentials
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import ir.amirab.downloader.utils.orDefault
import ir.amirab.util.flow.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddSingleDownloadComponent(
    ctx: ComponentContext,
    val onRequestClose: () -> Unit,
    val onRequestDownload: (DownloadItem, OnDuplicateStrategy) -> Unit,
    val onRequestAddToQueue: (DownloadItem, queueId: Long?, OnDuplicateStrategy) -> Unit,
    val openExistingDownload: (Long) -> Unit,
    id: String,
) : AddDownloadComponent(ctx, id),
    KoinComponent {

    private val appSettings: AppRepository by inject()
    private val client: DownloaderClient by inject()
    val downloadSystem: DownloadSystem by inject()

    private val downloadChecker = DownloadUiChecker(
        initialFolder = appSettings.saveLocation.value,
        downloadSystem = downloadSystem,
        scope = scope,
        downloaderClient = client,
        initialCredentials = DownloadCredentials.empty(),
    )

    //inputs
    val credentials = downloadChecker.credentials.asStateFlow()
    val name = downloadChecker.name.asStateFlow()
    val folder = downloadChecker.folder.asStateFlow()
    val onDuplicateStrategy: MutableStateFlow<OnDuplicateStrategy?> = MutableStateFlow(null)

    fun setCredentials(downloadCredentials: DownloadCredentials) {
        downloadChecker.credentials.update { downloadCredentials }
    }

    fun setFolder(folder: String) {
        downloadChecker.folder.update { folder }
    }

    fun setName(name: String) {
        downloadChecker.name.update { name }
    }

    fun setOnDuplicateStrategy(onDuplicateStrategy: OnDuplicateStrategy) {
        this.onDuplicateStrategy.update { onDuplicateStrategy }
    }

    init {
        merge(
            credentials.mapStateFlow { it.link },
            name,
            folder,
        )
            .onEachLatest { onDuplicateStrategy.update { null } }
            .launchIn(scope)
    }


    private val length: StateFlow<Long?> = downloadChecker.length
    val canAddResult = downloadChecker.canAddToDownloadResult.asStateFlow()
    private val canAdd = downloadChecker.canAdd
    private val isDuplicate = downloadChecker.isDuplicate
    val canAddToDownloads = combineStateFlows(
        canAdd, isDuplicate, onDuplicateStrategy
    ) { canAdd, isDuplicate, onDuplicateStrategy ->
        if (canAdd) {
            true
        } else if (isDuplicate && onDuplicateStrategy != null) {
            true
        } else {
            false
        }
    }

    val isLinkLoading = downloadChecker.gettingResponseInfo
    val linkResponseInfo = downloadChecker.responseInfo

    //extra settings
    private var threadCount = MutableStateFlow(null as Int?)
    private var speedLimit = MutableStateFlow(0L)


    val downloadItem = combineStateFlows(
        this.credentials,
        this.folder,
        this.name,
        this.length,
        this.speedLimit,
        this.threadCount
    ) { credentials,
        folder,
        name,
        length,
        speedLimit,
        threadCount ->
        DownloadItem(
            id = -1,
            folder = folder,
            name = name,
            link = credentials.link,
            contentLength = length ?: DownloadItem.LENGTH_UNKNOWN,
            dateAdded = 0,
            startTime = null,
            completeTime = null,
            status = DownloadStatus.Added,
            preferredConnectionCount = threadCount,
            speedLimit = speedLimit
        ).withCredentials(credentials)
    }


    var showMoreSettings by mutableStateOf(false)


    val configurables = listOf(
        SpeedLimitConfigurable(
            "Speed Limit",
            "Limit the speed of download for this file",
            backedBy = speedLimit,
            describe = {
                if (it == 0L) "Unlimited"
                else convertSpeedToHumanReadable(it)
            }
        ),
        IntConfigurable(
            "Thread count",
            "Limit the threads of download for this file",
            backedBy = threadCount.mapTwoWayStateFlow(
                map = {
                    it ?: 0
                },
                unMap = {
                    it.takeIf { it > 1 }
                }
            ),
            range = 0..32,
            describe = {
                if (it == 0) "use Global setting"
                else "$it thread for this download"
            }
        ),
        StringConfigurable(
            "Username",
            "username if the link is a protected resource",
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.username.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(username = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                ""
            }
        ),
        StringConfigurable(
            "Password",
            "Password if the link is a protected resource",
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.password.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(password = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                ""
            }
        ),
    )
    private val queueManager: QueueManager by inject()
    val queues = queueManager.queues
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    fun refresh() {
        downloadChecker.refresh()
    }

    fun onRequestDownload() {
        val item = downloadItem.value
        consumeDialog {
            addToLastUsedLocations(item.folder)
            onRequestDownload(item, onDuplicateStrategy.value.orDefault())
        }
    }

    fun onRequestAddToQueue(
        queueId: Long?,
    ) {
        val downloadItem = downloadItem.value
        consumeDialog {
            addToLastUsedLocations(downloadItem.folder)
            onRequestAddToQueue(downloadItem, queueId, onDuplicateStrategy.value.orDefault())
        }
    }

    fun openDownloadFileForCurrentLink() {
        (canAddResult.value as? CanAddResult.DownloadAlreadyExists)
            ?.itemId
            ?.let {
                openExistingDownload(it)
            }
    }

    var showSolutionsOnDuplicateDownloadUi by mutableStateOf(false)

    var shouldShowAddToQueue by mutableStateOf(false)

}