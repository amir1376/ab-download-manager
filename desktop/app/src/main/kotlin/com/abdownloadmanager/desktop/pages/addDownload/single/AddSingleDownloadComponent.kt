package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.DownloadUiChecker
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.runtime.*
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import com.abdownloadmanager.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.abdownloadmanager.utils.FileIconProvider
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.category.CategoryItem
import com.abdownloadmanager.utils.category.CategoryManager

sealed interface AddSingleDownloadPageEffects {
    data class SuggestUrl(val link: String) : AddSingleDownloadPageEffects
}

class AddSingleDownloadComponent(
    ctx: ComponentContext,
    val onRequestClose: () -> Unit,
    val onRequestDownload: OnRequestDownloadSingleItem,
    val onRequestAddToQueue: OnRequestAddSingleItem,
    val onRequestAddCategory: () -> Unit,
    val openExistingDownload: (Long) -> Unit,
    private val downloadItemOpener: DownloadItemOpener,
    id: String,
) : AddDownloadComponent(ctx, id),
    KoinComponent,
    ContainsEffects<AddSingleDownloadPageEffects> by supportEffects() {

    private val appSettings: AppRepository by inject()
    private val client: DownloaderClient by inject()
    val downloadSystem: DownloadSystem by inject()
    val iconProvider: FileIconProvider by inject()

    private val categoryManager: CategoryManager by inject()

    val categories = categoryManager.categoriesFlow
    private val _selectedCategory: MutableStateFlow<Category?> = MutableStateFlow(categories.value.firstOrNull())
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _useCategory = MutableStateFlow(false)
    val useCategory = _useCategory.asStateFlow()
    fun setUseCategory(value: Boolean) {
        _useCategory.update { value }
        if (value) {
            useCategoryFolder()
        } else {
            useDefaultFolder()
        }
    }

    private fun useCategoryFolder() {
        val category = selectedCategory.value
        if (useCategory.value && category != null) {
            setFolder(category.path)
        }
    }

    private fun useDefaultFolder() {
        setFolder(appSettings.saveLocation.value)
    }


    fun setSelectedCategory(category: Category) {
        _selectedCategory.update { category }
        if (useCategory.value) {
            useCategoryFolder()
        }
    }


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
        combine(
            name, credentials.map { it.link }
        ) { name, link ->
            val category = categoryManager.getCategoryOf(
                CategoryItem(
                    fileName = name,
                    url = link,
                )
            )
            if (category == null) {
                setUseCategory(false)
            } else {
                setUseCategory(true)
                setSelectedCategory(category)
            }
        }.launchIn(scope)
    }

    private var wasOpened = false
    fun onPageOpen() {
        if (wasOpened) return
        scope.launch {
            withContext(Dispatchers.Default) {
                // don't paste of link already exists
                // maybe a link already added by browser extension etc.
                if (credentials.value == DownloadCredentials.empty()) {
                    fillLinkIfThereIsALinkInClipboard()
                }
            }
        }
        wasOpened = true
    }

    private fun fillLinkIfThereIsALinkInClipboard() {
        val possibleLinks = ClipboardUtil.read() ?: return
        val downloadLinks = DownloadCredentialFromStringExtractor.extract(possibleLinks)
        if (downloadLinks.size == 1) {
            sendEffect(AddSingleDownloadPageEffects.SuggestUrl(downloadLinks[0].link))
        }
    }

    private val length: StateFlow<Long?> = downloadChecker.length
    val canAddResult = downloadChecker.canAddToDownloadResult.asStateFlow()
    private val canAdd = downloadChecker.canAdd
    private val isDuplicate = downloadChecker.isDuplicate

    val isLinkLoading = downloadChecker.gettingResponseInfo
    val linkResponseInfo = downloadChecker.responseInfo

    val canAddToDownloads = combineStateFlows(
        canAdd, isDuplicate, onDuplicateStrategy, isLinkLoading
    ) { canAdd, isDuplicate, onDuplicateStrategy, isLinkLoading ->
        if (isLinkLoading) {
            // link is loading wait for it...
            return@combineStateFlows false
        }
        if (canAdd) {
            true
        } else if (isDuplicate && onDuplicateStrategy != null) {
            true
        } else {
            false
        }
    }

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
    ) {
            credentials,
            folder,
            name,
            length,
            speedLimit,
            threadCount,
        ->
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
            saveLocationIfNecessary(item.folder)
            onRequestDownload(
                item,
                onDuplicateStrategy.value.orDefault(),
                selectedCategory.value?.id
            )
        }
    }

    private fun saveLocationIfNecessary(folder: String) {
        val category = selectedCategory.value?.takeIf {
            useCategory.value
        }
        val shouldAdd = if (category == null) {
            // always add if user don't use category
            true
        } else {
            // only add if category path is not the same as provided path
            category.path != folder
        }
        if (shouldAdd) {
            addToLastUsedLocations(folder)
        }
    }


    fun onRequestAddToQueue(
        queueId: Long?,
    ) {
        val downloadItem = downloadItem.value
        consumeDialog {
            saveLocationIfNecessary(downloadItem.folder)
            onRequestAddToQueue(
                downloadItem,
                queueId,
                onDuplicateStrategy.value.orDefault(),
                selectedCategory.value?.id
            )
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

    val shouldShowOpenFile = combine(
        onDuplicateStrategy, canAddResult,
    ) { onDuplicateStrategy, result ->
        if (result is CanAddResult.DownloadAlreadyExists && onDuplicateStrategy == null) {
            val item = downloadSystem.getDownloadItemById(result.itemId) ?: return@combine false
            if (item.status != DownloadStatus.Completed) {
                return@combine false
            }
            downloadSystem.getDownloadFile(item).exists()
        } else false
    }.stateIn(scope, SharingStarted.WhileSubscribed(), false)

    fun openExistingFile() {
        val itemId = (canAddResult.value as? CanAddResult.DownloadAlreadyExists)?.itemId ?: return
        consumeDialog {
            scope.launch {
                downloadItemOpener.openDownloadItem(itemId)
                onRequestClose()
            }
        }
    }

    fun addNewCategory() {
        onRequestAddCategory()
    }
}

fun interface OnRequestAddSingleItem {
    operator fun invoke(
        item: DownloadItem,
        queueId: Long?,
        onDuplicateStrategy: OnDuplicateStrategy,
        categoryId: Long?,
    )
}

fun interface OnRequestDownloadSingleItem {
    operator fun invoke(
        item: DownloadItem,
        onDuplicateStrategy: OnDuplicateStrategy,
        categoryId: Long?,
    )
}