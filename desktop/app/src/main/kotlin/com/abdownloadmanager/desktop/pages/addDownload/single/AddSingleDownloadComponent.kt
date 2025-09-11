package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.desktop.pages.addDownload.AddDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.DownloadUiChecker
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.runtime.*
import com.abdownloadmanager.desktop.pages.addDownload.ImportOptions
import com.abdownloadmanager.desktop.pages.addDownload.SilentImportOptions
import com.abdownloadmanager.desktop.pages.settings.ThreadCountLimitation
import com.abdownloadmanager.desktop.pages.settings.configurable.FileChecksumConfigurable
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.*
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.abdownloadmanager.shared.utils.extractors.linkextractor.DownloadCredentialFromStringExtractor
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
import com.abdownloadmanager.shared.utils.category.Category
import com.abdownloadmanager.shared.utils.category.CategoryItem
import com.abdownloadmanager.shared.utils.category.CategoryManager
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.utils.perhostsettings.applyToHttpDownload
import com.abdownloadmanager.shared.utils.perhostsettings.getSettingsForURL
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

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
    val updateExistingDownloadCredentials: (Long, IDownloadCredentials) -> Unit,
    private val downloadItemOpener: DownloadItemOpener,
    importOptions: ImportOptions,
    id: String,
) : AddDownloadComponent(ctx, id),
    KoinComponent,
    ContainsEffects<AddSingleDownloadPageEffects> by supportEffects() {

    private val appScope: CoroutineScope by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val appRepository: AppRepository by inject()
    private val perHostSettingsManager: PerHostSettingsManager by inject()
    private val client: DownloaderClient by inject()
    val downloadSystem: DownloadSystem by inject()
    val iconProvider: FileIconProvider by inject()
    private val _shouldShowWindow = MutableStateFlow(importOptions.silentImport == null)
    override val shouldShowWindow: StateFlow<Boolean> = _shouldShowWindow.asStateFlow()

    private val categoryManager: CategoryManager by inject()

    val categories = categoryManager.categoriesFlow
    private val _selectedCategory: MutableStateFlow<Category?> = MutableStateFlow(categories.value.firstOrNull())
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _useCategory = MutableStateFlow(false)
    val useCategory = _useCategory.asStateFlow()
    fun setUseCategory(useCategory: Boolean) {
        _useCategory.update { useCategory }
        if (useCategory) {
            val usedCategoryFolder = useCategoryFolder(_useCategory.value)
            if (!usedCategoryFolder) {
                useDefaultFolder()
            }
        } else {
            useDefaultFolder()
        }
    }

    private fun useCategoryFolder(
        useCategory: Boolean,
    ): Boolean {
        val category = selectedCategory.value
        if (useCategory && category != null) {
            category.getDownloadPath()?.let {
                setFolder(it)
                return true
            }
        }
        return false
    }

    private fun useDefaultFolder() {
        setFolder(appRepository.saveLocation.value)
    }


    fun setSelectedCategory(category: Category) {
        _selectedCategory.update { category }
        val useCategory = useCategory.value
        if (useCategory) {
            val used = useCategoryFolder(useCategory)
            if (!used) {
                useDefaultFolder()
            }
        }
    }


    private val downloadChecker = DownloadUiChecker(
        initialFolder = appRepository.saveLocation.value,
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
        credentials
            .map { it.link }
            .distinctUntilChanged()
            .debounce(250)
            .onEachLatest { link ->
                perHostSettingsManager
                    .getSettingsForURL(link)
                    ?.let(::applyHostSettingsToExtraConfig)
            }
            .flowOn(Dispatchers.IO)
            .launchIn(scope)
        merge(
            credentials.mapStateFlow { it.link },
            name,
            folder,
        )
            .onEachLatest { onDuplicateStrategy.update { null } }
            .launchIn(scope)
        combine(
            name,
            credentials.map { it.link },
        ) { name, link ->
            val category = categoryManager.getCategoryOf(
                CategoryItem(
                    fileName = name,
                    url = link,
                )
            )
            val globalUseCategoryByDefault = appSettings.useCategoryByDefault.value
            val suggestedUseCategory: Boolean
            if (category == null) {
                suggestedUseCategory = false
            } else {
                setSelectedCategory(category)
                suggestedUseCategory = true
            }
            if (globalUseCategoryByDefault) {
                setUseCategory(suggestedUseCategory)
            }
        }.launchIn(scope)
    }

    private fun applyHostSettingsToExtraConfig(extraConfig: PerHostSettingsItem) {
        extraConfig.applyToHttpDownload(
            setUsername = { setCredentials(credentials.value.copy(username = it)) },
            setPassword = { setCredentials(credentials.value.copy(password = it)) },
            setUserAgent = { setCredentials(credentials.value.copy(userAgent = it)) },
            setThreadCount = { threadCount.value = it },
            setSpeedLimit = { speedLimit.value = it }
        )
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
    private var fileChecksum = MutableStateFlow(null as FileChecksum?)


    val downloadItem = combineStateFlows(
        this.credentials,
        this.folder,
        this.name,
        this.length,
        this.speedLimit,
        this.threadCount,
        this.fileChecksum,
    ) {
            credentials,
            folder,
            name,
            length,
            speedLimit,
            threadCount,
            fileChecksum,
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
            speedLimit = speedLimit,
            fileChecksum = fileChecksum?.toString()
        ).withCredentials(credentials)
    }


    var showMoreSettings by mutableStateOf(false)


    val configurables = listOf(
        SpeedLimitConfigurable(
            Res.string.download_item_settings_speed_limit.asStringSource(),
            Res.string.download_item_settings_speed_limit_description.asStringSource(),
            backedBy = speedLimit,
            describe = {
                if (it == 0L) Res.string.unlimited.asStringSource()
                else convertPositiveSpeedToHumanReadable(
                    it, appRepository.speedUnit.value
                ).asStringSource()
            }
        ),
        FileChecksumConfigurable(
            Res.string.download_item_settings_file_checksum.asStringSource(),
            Res.string.download_item_settings_file_checksum_description.asStringSource(),
            backedBy = fileChecksum,
            describe = { "".asStringSource() }
        ),
        IntConfigurable(
            Res.string.settings_download_thread_count.asStringSource(),
            Res.string.settings_download_thread_count_description.asStringSource(),
            backedBy = threadCount.mapTwoWayStateFlow(
                map = {
                    it ?: 0
                },
                unMap = {
                    it.takeIf { it >= 1 }
                }
            ),
            range = 0..ThreadCountLimitation.MAX_ALLOWED_THREAD_COUNT,
            describe = {
                if (it == 0) Res.string.use_global_settings.asStringSource()
                else Res.string.download_item_settings_thread_count_describe
                    .asStringSourceWithARgs(
                        Res.string.download_item_settings_thread_count_describe_createArgs(
                            count = it.toString()
                        )
                    )
            }
        ),
        StringConfigurable(
            Res.string.username.asStringSource(),
            Res.string.download_item_settings_username_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.username.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(username = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.password.asStringSource(),
            Res.string.download_item_settings_password_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.password.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(password = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                "".asStringSource()
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
                getCategoryIfUseCategoryIsOn()?.id
            )
        }
    }

    private fun getCategoryIfUseCategoryIsOn(): Category? {
        return if (useCategory.value)
            selectedCategory.value
        else
            null
    }

    private fun saveLocationIfNecessary(folder: String) {
        val category = getCategoryIfUseCategoryIsOn()
        val shouldAdd = if (category == null) {
            // always add if user don't use category
            true
        } else {
            // only add if category path is not the same as provided path
            category.getDownloadPath() != folder
        }
        if (shouldAdd) {
            addToLastUsedLocations(folder)
        }
    }


    fun onRequestAddToQueue(
        queueId: Long?,
        startQueue: Boolean,
    ) {
        val downloadItem = downloadItem.value
        consumeDialog {
            saveLocationIfNecessary(downloadItem.folder)
            onRequestAddToQueue(
                item = downloadItem,
                queueId = queueId,
                onDuplicateStrategy = onDuplicateStrategy.value.orDefault(),
                categoryId = getCategoryIfUseCategoryIsOn()?.id,
            ).invokeOnCompletion {
                if (queueId != null && startQueue) {
                    GlobalScope.launch {
                        downloadSystem.startQueue(queueId)
                    }
                }
            }
            onRequestClose()
        }
    }

    fun openDownloadFileForCurrentLink() {
        (canAddResult.value as? CanAddResult.DownloadAlreadyExists)
            ?.itemId
            ?.let {
                openExistingDownload(it)
            }
    }

    fun updateDownloadCredentialsOfOriginalDownload() {
        (canAddResult.value as? CanAddResult.DownloadAlreadyExists)
            ?.itemId
            ?.let {
                updateExistingDownloadCredentials(it, downloadItem.value)
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
            appScope.launch {
                downloadItemOpener.openDownloadItem(itemId)
            }
            onRequestClose()
        }
    }

    fun addNewCategory() {
        onRequestAddCategory()
    }

    init {
        importOptions.silentImport?.let {
            handleSilentImport(it)
        }
    }

    fun handleSilentImport(silentImport: SilentImportOptions) {
        scope.launch {
            try {
                withTimeout(2_000) {
                    // ensure all values are set!
                    credentials.map { it.link }.first { it.isNotEmpty() }
                    folder.first { it.isNotEmpty() }
                    name.first { it.isNotEmpty() }
                }
            } catch (_: Exception) {
                onRequestClose()
                return@launch
            }
            val failAutoAdd = async {
                try {
                    // although we don't need timeout, but I add this timeout here maybe there is a bug
                    // and I don't want this coroutine to be halted infinitely
                    withTimeout(10_000) {
                        canAddToDownloads.first { it }
                        if (silentImport.silentDownload) {
                            onRequestDownload()
                        } else {
                            onRequestAddToQueue(
                                DefaultQueueInfo.ID,
                                false,
                            )
                        }
                    }
                    false
                } catch (_: Exception) {
                    true
                }
            }

            val errorDuringWait = async {
                val channel = canAddResult.produceIn(this)
                try {
                    val startTime = System.currentTimeMillis()
                    for (i in channel) {
                        when (i) {
                            is CanAddResult.DownloadAlreadyExists,
                            CanAddResult.CantWriteInThisFolder -> {
                                return@async true
                            }

                            CanAddResult.InvalidUrl,
                            CanAddResult.InvalidFileName -> {
                                // we may get invalid filename/invalid url at the beginning! because the name is empty
                                if (System.currentTimeMillis() - startTime >= 1000) {
                                    return@async true
                                }
                            }

                            CanAddResult.CanAdd -> {
                                // we must not break here because it cancels [failAutoAdd]
                                // instead we wait for [failAutoAdd] to be finished and we will be cancelled automatically after select is done!
                            }

                            null -> {}
                        }
                    }
                    return@async true
                } finally {
                    channel.cancel()
                }
            }
            val failedToAutoAdd = try {
                select {
                    failAutoAdd.onAwait { failed ->
                        failed
                    }
                    errorDuringWait.onAwait { errorDuringWait ->
                        errorDuringWait
                    }
                }
            } catch (_: Exception) {
                true
            } finally {
                runCatching {
                    failAutoAdd.cancelAndJoin()
                    errorDuringWait.cancelAndJoin()
                }
            }
            if (failedToAutoAdd) {
                // needs adjustments by user!
                _shouldShowWindow.value = true
            }
        }
    }
}

fun interface OnRequestAddSingleItem {
    operator fun invoke(
        item: DownloadItem,
        queueId: Long?,
        onDuplicateStrategy: OnDuplicateStrategy,
        categoryId: Long?,
    ): Deferred<Long>
}

fun interface OnRequestDownloadSingleItem {
    operator fun invoke(
        item: DownloadItem,
        onDuplicateStrategy: OnDuplicateStrategy,
        categoryId: Long?,
    )
}
