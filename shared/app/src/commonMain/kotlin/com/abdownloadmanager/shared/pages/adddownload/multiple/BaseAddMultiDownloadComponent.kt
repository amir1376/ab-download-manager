package com.abdownloadmanager.shared.pages.adddownload.multiple

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import arrow.core.Some
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.add.TANewDownloadInputs
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryItem
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.CategorySelectionMode
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.perhostsettings.getSettingsForURL
import com.arkivanov.decompose.ComponentContext
import ir.amirab.SelectionUtil
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.forEach

abstract class BaseAddMultiDownloadComponent(
    ctx: ComponentContext,
    id: String,
    private val onRequestClose: () -> Unit,
    private val onRequestAdd: OnRequestAdd,
    private val appRepository: BaseAppRepository,
    private val perHostSettingsManager: PerHostSettingsManager,
    val downloadSystem: DownloadSystem,
    val fileIconProvider: FileIconProvider,
    private val categoryManager: CategoryManager,
    val downloaderInUiRegistry: DownloaderInUiRegistry,
    protected val queueManager: QueueManager,
    lastSavedLocationsStorage: ILastSavedLocationsStorage,
) : AddDownloadComponent(ctx, id, lastSavedLocationsStorage) {
    override val shouldShowWindow: StateFlow<Boolean> = MutableStateFlow(true)

    private val _folder = MutableStateFlow(appRepository.saveLocation.value)
    val folder = _folder.asStateFlow()
    fun setFolder(folder: String) {
        this._folder.update { folder }
        list.forEach {
            it.folder.update { folder }
        }
    }

    // when we select all files in one location let user option to auto categorize items
    private val _alsoAutoCategorize = MutableStateFlow(true)
    val alsoAutoCategorize = _alsoAutoCategorize.asStateFlow()
    fun setAlsoAutoCategorize(value: Boolean) {
        _alsoAutoCategorize.update { value }
    }

    val categories = categoryManager.categoriesFlow
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.update {
            category
        }
    }

    private val _allInSameLocation = MutableStateFlow(false)
    val allInSameLocation = _allInSameLocation.asStateFlow()

    fun setAllItemsInSameLocation(sameLocation: Boolean) {
        _allInSameLocation.update { sameLocation }
    }



    private fun newCheckerWithInputs(
        addDownloadCredentialsInUiProps: AddDownloadCredentialsInUiProps
    ): TANewDownloadInputs? {
        val iDownloadCredentials = addDownloadCredentialsInUiProps.credentials
        return downloaderInUiRegistry
            .getDownloaderOf(iDownloadCredentials)
            ?.createNewDownloadInputs(
                initialCredentials = iDownloadCredentials,
                initialName = addDownloadCredentialsInUiProps.extraConfig.getAndFixSuggestedName().orEmpty(),
                initialFolder = folder.value,
                downloadSystem = downloadSystem,
                scope = scope,
            )
    }

    fun addItems(list: List<AddDownloadCredentialsInUiProps>) {
        val newItemsToAdd = list.filter {
            it.credentials !in this.list.map {
                it.credentials.value
            }
        }.mapNotNull {
            newCheckerWithInputs(it)
                ?.also { inputComponent ->
                    val perHostSettingsItem = perHostSettingsManager
                        .getSettingsForURL(it.credentials.link)
                    perHostSettingsItem?.let {
                        inputComponent
                            .applyHostSettingsToExtraConfig(perHostSettingsItem)
                    }
                }
        }
        enqueueCheck(newItemsToAdd)
        this.list = this.list.plus(newItemsToAdd)
    }

    var list: List<TANewDownloadInputs> by mutableStateOf(emptyList())

    private val checkList = MutableSharedFlow<TANewDownloadInputs>()
    private fun enqueueCheck(links: List<TANewDownloadInputs>) {
        scope.launch {
            for (i in links) {
                checkList.emit(i)
            }
        }
    }

    init {
        checkList.onEach {
            it.downloadUiChecker.refresh()
        }
            .launchIn(scope)
    }

    var selectionList by mutableStateOf<List<String>>(emptyList())
    fun isSelected(item: TANewDownloadInputs): Boolean {
        return item.credentials.value.link in selectionList
    }

    val isAllSelected by derivedStateOf {
        list.all { it.credentials.value.link in selectionList }
    }

    var lastSelectedId by mutableStateOf(null as String?)

    fun setSelect(id: String, selected: Boolean) {
        if (selected) {
            lastSelectedId = id
            if (!selectionList.contains(id)) {
                selectionList = selectionList.plus(id)
            }
        } else {
            selectionList = selectionList.minus(id)
        }
    }

    fun resetSelectionTo(ids: List<String>, boolean: Boolean) {
        selectionList = ids.takeIf { boolean }
            .orEmpty()
    }

    fun selectAll(value: Boolean) {
        selectionList = if (value) {
            list.map { it.credentials.value.link }
        } else {
            emptyList()
        }
    }

    fun toggleSelectInside() {
        SelectionUtil.toggleSelectInside(
            selectionList = selectionList,
            fullSortedList = list,
            getId = {
                it.credentials.value.link
            }
        )?.let {
            selectionList = it
        }
    }

    fun inverseSelection() {
        selectionList = SelectionUtil.invertSelection(
            selectionList = selectionList,
            all = list,
            getId = {
                it.credentials.value.link
            }
        )
    }

    val canClickAdd by derivedStateOf {
        selectionList.isNotEmpty()
    }
    val queueList = queueManager.queues

    private fun getFolderForItem(
        categorySelectionMode: CategorySelectionMode?,
        allInSameLocation: Boolean,
        url: String,
        fleName: String,
        defaultFolder: String,
    ): String {
        if (allInSameLocation) return defaultFolder
        return when (categorySelectionMode) {
            CategorySelectionMode.Auto -> {
                downloadSystem.categoryManager
                    .getCategoryOf(
                        CategoryItem(
                            url = url,
                            fileName = fleName,
                        )
                    )?.getDownloadPath()
                    ?: defaultFolder
            }

            is CategorySelectionMode.Fixed -> {
                downloadSystem.categoryManager
                    .getCategoryById(categorySelectionMode.categoryId)?.getDownloadPath()
                    ?: defaultFolder
            }

            null -> defaultFolder
        }
    }

    fun requestAddDownloads(
        queueId: Long?, startQueue: Boolean,
    ) {

        val categorySelectionMode = when {
            alsoAutoCategorize.value -> CategorySelectionMode.Auto
            else -> selectedCategory.value?.let {
                CategorySelectionMode.Fixed(it.id)
            }
        }
        val itemsToAdd = list
            .filter { it.credentials.value.link in selectionList }
            .filter {
                val checker = it.downloadUiChecker
                checker.canAdd.value
                        || checker.isDuplicate.value // we add numbered file strategy
            }
            .map {
                NewDownloadItemProps(
                    downloadItem = it.downloadItem.value.copy(
                        folder = Some(
                            getFolderForItem(
                                categorySelectionMode = categorySelectionMode,
                                url = it.credentials.value.link,
                                fleName = it.name.value,
                                defaultFolder = it.folder.value,
                                allInSameLocation = allInSameLocation.value
                            )
                        )
                    ),
                    extraConfig = it.downloadJobConfig.value,
                    onDuplicateStrategy = OnDuplicateStrategy.AddNumbered,
                    context = EmptyContext,
                )
            }
        consumeDialog {
            onRequestAdd(
                items = itemsToAdd,
                queueId = queueId,
                categorySelectionMode = categorySelectionMode
            ).invokeOnCompletion {
                val folder = folder.value
                if (allInSameLocation.value) {
                    addToLastUsedLocations(folder)
                }
                if (startQueue && queueId != null) {
                    scope.launch {
                        downloadSystem.startQueue(queueId)
                    }
                }
            }
            requestClose()
        }
    }

    var showAddToQueue by mutableStateOf(false)
        private set

    fun getIdOf(item: TANewDownloadInputs): Int {
        return item.getUniqueId()
    }

    fun openConfigurableList(
        itemID: Int?
    ) {
        currentDownloadConfigurableList.value = itemID?.let { id ->
            list.find { getIdOf(it) == id }
        }?.configurableList
    }

    val currentDownloadConfigurableList: MutableStateFlow<List<Configurable<*>>?> = MutableStateFlow(null)

    fun openAddToQueueDialog() {
        showAddToQueue = true
    }

    fun closeAddToQueue() {
        showAddToQueue = false
    }

    fun requestClose() {
        onRequestClose()
    }
}
