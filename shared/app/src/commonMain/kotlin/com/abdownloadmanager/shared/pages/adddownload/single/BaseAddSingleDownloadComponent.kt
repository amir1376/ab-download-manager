package com.abdownloadmanager.shared.pages.adddownload.single

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.add.CanAddResult
import com.abdownloadmanager.shared.pagemanager.DownloadErrorDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.FolderChangeResult
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.shared.pages.adddownload.SilentImportOptions
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.appsettings.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.FilenameFixer
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryItem
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.perhostsettings.getSettingsForURL
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import ir.amirab.downloader.utils.orDefault
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.onEachLatest
import ir.amirab.util.toSingleLine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class BaseAddSingleDownloadComponent(
    ctx: ComponentContext,
    val onRequestClose: () -> Unit,
    val onRequestDownload: OnRequestDownloadSingleItem,
    private val onRequestAddToQueue: OnRequestAddSingleItem,
    val openExistingDownload: (Long) -> Unit,
    val updateExistingDownloadCredentials: (Long, IDownloadCredentials, DownloadJobExtraConfig?) -> Unit,
    protected val downloadItemOpener: DownloadItemOpener,
    protected val downloadErrorDialogManager: DownloadErrorDialogManager,
    protected val lastSavedLocationsStorage: ILastSavedLocationsStorage,
    protected val appScope: CoroutineScope,
    protected val notificationSender: NotificationSender,
    protected val appSettings: BaseAppSettingsStorage,
    appRepository: BaseAppRepository,
    protected val perHostSettingsManager: PerHostSettingsManager,
    categoryManager: CategoryManager,
    val downloadSystem: DownloadSystem,
    val iconProvider: FileIconProvider,
    selectQueueStorage: ISelectQueueStorage,
    queueManager: QueueManager,
    importOptions: ImportOptions,
    id: String,
    downloaderInUi: DownloaderInUi<IDownloadCredentials, *, *, *, *, *, *, *, *, *>,
    initialCredentials: AddDownloadCredentialsInUiProps,
) : AddDownloadComponent(
    ctx = ctx,
    id = id,
    lastSavedLocationsStorage = lastSavedLocationsStorage,
    queueManager = queueManager,
    selectQueueStorage = selectQueueStorage,
    appRepository = appRepository,
    categoryManager = categoryManager,
),
    ContainsEffects<BaseAddSingleDownloadComponent.Effects> by supportEffects() {
    private val _shouldShowWindow = MutableStateFlow(importOptions.silentImport == null)
    override val shouldShowWindow: StateFlow<Boolean> = _shouldShowWindow.asStateFlow()

    /**
     * External requests (browser integration, share intents, ...) may be submitted right away:
     * the link check and the submission are done in the background by [appScope]
     * instead of keeping the dialog disabled until the link is checked.
     */
    private val backgroundSubmitEnabled =
        importOptions.externalRequest && appSettings.backgroundAddExternalDownloads.value

    /**
     * The name we can show until the link check resolves the real file name.
     */
    private val nameFromLink: String = HttpUrlUtils.extractNameFromLink(initialCredentials.credentials.link)
        ?.let(FilenameFixer::fix)
        .orEmpty()

    private val initialName: String = initialCredentials.extraConfig.getAndFixSuggestedName()
        .orEmpty()
        .let { suggestedName ->
            // the name derived from the link is only a fallback for externally launched dialogs,
            // the internal "new download" dialog keeps its previous behavior
            if (importOptions.externalRequest) {
                suggestedName.ifBlank { nameFromLink }
            } else {
                suggestedName
            }
        }

    val downloadInputsComponent = downloaderInUi.createNewDownloadInputs(
        initialFolder = appRepository.saveLocation.value,
        initialName = initialName,
        downloadSystem = downloadSystem,
        scope = scope,
        initialCredentials = initialCredentials.credentials,
    )
    val downloadChecker = downloadInputsComponent.newDownloadUiChecker

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

    fun openDownloadErrorDialog() {
        downloadChecker.lastErrorReason.value
            ?.let { reason ->
                downloadErrorDialogManager.openDownloadErrorDialog(
                    downloadItem.value,
                    reason,
                )
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

    override val isFolderChangedResult: StateFlow<FolderChangeResult> = combine(
        folder,
        useCategory,
        selectedCategory,
        categories,
        appRepository.saveLocation,
    ) { newSelectedFolder, useCategory, selectedCat, allCategories, defaultLocation ->
        val category = if (useCategory) {
            selectedCat?.let { cat -> allCategories.find { it.id == cat.id } ?: cat }
        } else null
        val categoryOrDefaultFolder = category?.getDownloadPath() ?: defaultLocation
        FolderChangeResult.of(
            category = category,
            userSelectedFolder = newSelectedFolder,
            currentFolder = categoryOrDefaultFolder,
        )
    }.stateIn(scope, SharingStarted.Eagerly, FolderChangeResult.default())

    fun setCredentials(downloadCredentials: IDownloadCredentials) {
        downloadChecker.credentials.update { downloadCredentials }
    }

    fun setFolder(folder: String) {
        downloadChecker.folder.update { folder }
    }

    fun setName(name: String) {
        val refinedName = name
            .toSingleLine()
        downloadChecker.name.update { refinedName }
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
            .debounce(250.milliseconds)
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
    val checkResponseResult = downloadChecker.responseResult
    val lastError = downloadChecker.lastErrorReason

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

    /**
     * Whether the "Download" action can be triggered right now.
     * For external requests the link check is deferred to a background task,
     * so this action is available without waiting for it.
     */
    val canSubmitDownload: StateFlow<Boolean> = if (backgroundSubmitEnabled) {
        MutableStateFlow(true)
    } else {
        canAddToDownloads
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
        if (backgroundSubmitEnabled) {
            onRequestBackgroundDownload()
        } else {
            consumeDialog {
                submitDownload()
                onRequestClose()
            }
        }
    }

    /**
     * Closes the dialog right away and does the rest of the job in the background:
     * wait (at most [backgroundSubmitLinkCheckTimeout]) for the link check to resolve
     * the file name/size, then add and start the download without any further user interaction.
     * The whole job is bounded by [backgroundSubmitOverallTimeout].
     */
    private fun onRequestBackgroundDownload() {
        consumeDialog {
            // the window closes right away, the rest happens in the background
            setShouldShowWindow(false)
            appScope.launch {
                submitDownloadInBackground()
            }
        }
    }

    protected fun setShouldShowWindow(value: Boolean) {
        _shouldShowWindow.value = value
    }

    /** Builds the download item from the current inputs and submits it. */
    private fun submitDownload() {
        val downloadItem = this@BaseAddSingleDownloadComponent.downloadItem.value
        val downloadJobExtraConfig = downloadJobConfig.value
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
    }

    private suspend fun submitDownloadInBackground() {
        // The whole background job is bounded: neither a hung link check nor a hung validation
        // may keep the (hidden) dialog alive. Its expiry is handled like a link probe timeout.
        val linkInfoResolved = withTimeoutOrNull(backgroundSubmitOverallTimeout) {
            resolveLinkInfoInBackground()
        }
        // the link check may have resolved the name right before the job was cut short
        val linkResolved = linkInfoResolved ?: (checkResponseResult.value?.isSuccess == true)
        when (canAddResult.value) {
            // an unknown result (the job timed out or the validation never settled) is handled
            // like a link probe timeout: add the download with the name we derived from the link.
            // A known local error is never submitted.
            CanAddResult.CanAdd, null -> submitBackgroundDownload(linkResolved)

            else -> reportBackgroundSubmitFailure()
        }
    }

    /** Submits the download, closes the dialog and reports the outcome to the user. */
    private fun submitBackgroundDownload(linkResolved: Boolean) {
        val submittedName = downloadItem.value.name
        submitDownload()
        onRequestClose()
        notificationSender.sendNotification(
            tag = id,
            title = if (linkResolved) {
                Res.string.download_added
            } else {
                Res.string.download_added_name_may_be_inaccurate
            }.asStringSource(),
            description = submittedName.asStringSource(),
            type = if (linkResolved) NotificationType.Success else NotificationType.Warning,
        )
    }

    /**
     * Waits for the link check to resolve the file name/size and then re-validates the inputs.
     *
     * @return true when the link info was resolved (the name is then the real file name).
     */
    private suspend fun resolveLinkInfoInBackground(): Boolean {
        if (checkResponseResult.value == null) {
            val resolvedName = withTimeoutOrNull(backgroundSubmitLinkCheckTimeout) {
                awaitLinkCheckName()
            }
            if (resolvedName != null && !userChangedTheName()) {
                setName(resolvedName)
            }
        }
        // must run even when the link check timed out: local errors have to be detected in any case
        runCatching { downloadChecker.revalidateNow() }
        // The result is reset to null whenever the name/folder change and those collectors run on
        // another dispatcher, so a single read right after the revalidation could observe null
        // (or a stale value) and reject a perfectly valid download. Wait for a settled result.
        withTimeoutOrNull(backgroundSubmitValidationTimeout) {
            canAddResult.filterNotNull().first()
        }
        return checkResponseResult.value?.isSuccess == true
    }

    /**
     * The name resolved by the link check.
     * A check that is already in flight (the dialog's own collectors start one when it opens)
     * is awaited instead of starting a redundant second probe.
     */
    private suspend fun awaitLinkCheckName(): String? {
        if (!downloadChecker.gettingResponseInfo.value) {
            return downloadChecker.checkLinkNow()
        }
        downloadChecker.gettingResponseInfo.first { !it }
        // the response result is published right after the loading flag goes down
        checkResponseResult.first { it != null }
        return downloadChecker.name.value.takeIf { it != initialName }
    }

    /**
     * The name resolved by the link check only replaces the one we derived from the link,
     * a name that was set by the user is kept.
     */
    private fun userChangedTheName(): Boolean {
        return name.value.ifBlank { initialName } != initialName
    }

    /**
     * Hard local errors (invalid url/file name, folder is not writable, confirmed duplicate)
     * must not be submitted. Re-open the dialog so the user can fix the inputs,
     * or notify when the dialog can't be opened any more.
     */
    private fun reportBackgroundSubmitFailure() {
        if (isAppVisibleToUser() && reopenDialogWithError()) {
            return
        }
        notificationSender.sendNotification(
            tag = id,
            title = Res.string.cant_add_download.asStringSource(),
            description = describeBackgroundSubmitFailure(canAddResult.value),
            type = NotificationType.Error,
        )
        onRequestClose()
    }

    /**
     * Shows the (still alive, but hidden) dialog again, prefilled with the inputs and
     * the error that prevented the submission.
     */
    private fun reopenDialogWithError(): Boolean {
        if (!scope.isActive) return false
        // the user may submit again after fixing the inputs
        makeDialogConsumableAgain()
        setShouldShowWindow(true)
        return true
    }

    private fun describeBackgroundSubmitFailure(result: CanAddResult?): StringSource {
        return when (result) {
            CanAddResult.InvalidUrl -> Res.string.invalid_url
            CanAddResult.InvalidFileName -> Res.string.invalid_file_name
            CanAddResult.CantWriteInThisFolder -> Res.string.cant_write_to_this_folder
            is CanAddResult.DownloadAlreadyExists -> Res.string.download_already_exists
            CanAddResult.CanAdd, null -> Res.string.unknown_error
        }.asStringSource()
    }

    /**
     * Whether the app is in the foreground right now.
     * When it isn't, background failures are reported using a notification.
     */
    protected open fun isAppVisibleToUser(): Boolean = true

    private fun getCategoryIfUseCategoryIsOn(): Category? {
        return if (useCategory.value)
            selectedCategory.value
        else
            null
    }

    private fun saveLocationIfNecessary(folder: String) {
        val category = getCategoryIfUseCategoryIsOn()
        if (rememberFolderAsDefault.value && isFolderChangedResult.value.isChanged) {
            persistFolder(folder, category)
        } else {
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
    }


    override fun onRequestAddToQueue(
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
                withTimeout(2.seconds) {
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
                    withTimeout(10.seconds) {
                        canAddToDownloads.first { it }
                        if (silentImport.silentDownload) {
                            onRequestDownload()
                        } else {
                            selectQueueComponent.fastConfirm()
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

    companion object {
        /**
         * How long a background submission waits for the link check to resolve the file name/size.
         */
        private val backgroundSubmitLinkCheckTimeout = 15.seconds

        /**
         * How long a background submission waits for the validation to settle after the link check.
         */
        private val backgroundSubmitValidationTimeout = 2.seconds

        /**
         * Upper bound of the whole background submission: when it expires the download is added
         * with the name derived from the link, so the hidden dialog can't stay alive forever.
         */
        private val backgroundSubmitOverallTimeout = 20.seconds
    }

    sealed interface Effects {
        sealed interface Common : Effects {
            data class SuggestUrl(val link: String) : Common
        }

        interface Platform : Effects
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
