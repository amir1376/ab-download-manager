package com.abdownloadmanager.shared.pages.adddownload.single

import com.abdownloadmanager.shared.pages.adddownload.AddDownloadComponent
import androidx.compose.runtime.*
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.shared.pages.adddownload.SilentImportOptions
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.shared.util.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import ir.amirab.downloader.utils.orDefault
import ir.amirab.util.flow.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryItem
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.downloaderinui.add.CanAddResult
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.perhostsettings.getSettingsForURL
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import org.koin.core.component.inject
import kotlin.getValue

sealed interface AddSingleDownloadPageEffects {
    data class SuggestUrl(val link: String) : AddSingleDownloadPageEffects
}

abstract class BaseAddSingleDownloadComponent(
    ctx: ComponentContext,
    val onRequestClose: () -> Unit,
    val onRequestDownload: OnRequestDownloadSingleItem,
    private val onRequestAddToQueue: OnRequestAddSingleItem,
    val openExistingDownload: (Long) -> Unit,
    val updateExistingDownloadCredentials: (Long, IDownloadCredentials, DownloadJobExtraConfig?) -> Unit,
    protected val downloadItemOpener: DownloadItemOpener,
    protected val lastSavedLocationsStorage: ILastSavedLocationsStorage,
    protected val appScope: CoroutineScope,
    protected val appSettings: BaseAppSettingsStorage,
    protected val appRepository: BaseAppRepository,
    protected val perHostSettingsManager: PerHostSettingsManager,
    protected val categoryManager: CategoryManager,
    val downloadSystem: DownloadSystem,
    val iconProvider: FileIconProvider,
    protected val queueManager: QueueManager,
    importOptions: ImportOptions,
    id: String,
    downloaderInUi: DownloaderInUi<IDownloadCredentials, *, *, *, *, *, *, *, *>,
    initialCredentials: AddDownloadCredentialsInUiProps,
) : AddDownloadComponent(ctx, id, lastSavedLocationsStorage),
    ContainsEffects<AddSingleDownloadPageEffects> by supportEffects() {
    private val _shouldShowWindow = MutableStateFlow(importOptions.silentImport == null)
    override val shouldShowWindow: StateFlow<Boolean> = _shouldShowWindow.asStateFlow()
    val downloadInputsComponent = downloaderInUi.createNewDownloadInputs(
        initialFolder = appRepository.saveLocation.value,
        initialName = initialCredentials.extraConfig.getAndFixSuggestedName().orEmpty(),
        downloadSystem = downloadSystem,
        scope = scope,
        initialCredentials = initialCredentials.credentials,
    )
    val downloadChecker = downloadInputsComponent.downloadUiChecker

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


    //inputs
    val credentials = downloadChecker.credentials.asStateFlow()
    val name = downloadChecker.name.asStateFlow()
    val folder = downloadChecker.folder.asStateFlow()
    val onDuplicateStrategy: MutableStateFlow<OnDuplicateStrategy?> = MutableStateFlow(null)

    fun setCredentials(downloadCredentials: IDownloadCredentials) {
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

    fun getLengthString(): StringSource {
        return downloadInputsComponent.getLengthString()
    }

    init {
        credentials
            .map { it.link }
            .distinctUntilChanged()
            .debounce(250)
            .onEachLatest { link ->
                perHostSettingsManager
                    .getSettingsForURL(link)
                    ?.let(downloadInputsComponent::applyHostSettingsToExtraConfig)
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

    val downloadItem = downloadInputsComponent.downloadItem
    val downloadJobConfig = downloadInputsComponent.downloadJobConfig


    var showMoreSettings by mutableStateOf(false)


    val configurables = downloadInputsComponent.configurableList

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
        val downloadItem = this@BaseAddSingleDownloadComponent.downloadItem.value
        val downloadJobExtraConfig = downloadJobConfig.value
        consumeDialog {
            saveLocationIfNecessary(downloadItem.folder)
            onRequestDownload(
                item = NewDownloadItemProps(
                    downloadItem = downloadItem,
                    extraConfig = downloadJobExtraConfig,
                    onDuplicateStrategy = onDuplicateStrategy.value.orDefault(),
                    context = EmptyContext
                ),
                categoryId = getCategoryIfUseCategoryIsOn()?.id
            )
            onRequestClose()
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
        val downloadJobConfig = downloadJobConfig.value
        consumeDialog {
            saveLocationIfNecessary(downloadItem.folder)
            onRequestAddToQueue(
                item = NewDownloadItemProps(
                    downloadItem = downloadItem,
                    extraConfig = downloadJobConfig,
                    onDuplicateStrategy = onDuplicateStrategy.value.orDefault(),
                    context = EmptyContext,
                ),
                queueId = queueId,
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
                onRequestClose()
            }
    }

    fun updateDownloadCredentialsOfOriginalDownload() {
        (canAddResult.value as? CanAddResult.DownloadAlreadyExists)
            ?.itemId
            ?.let {
                updateExistingDownloadCredentials(it, downloadItem.value, downloadJobConfig.value)
                onRequestClose()
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
        item: NewDownloadItemProps,
        queueId: Long?,
        categoryId: Long?,
    ): Deferred<Long>
}

fun interface OnRequestDownloadSingleItem {
    operator fun invoke(
        item: NewDownloadItemProps,
        categoryId: Long?,
    )
}
