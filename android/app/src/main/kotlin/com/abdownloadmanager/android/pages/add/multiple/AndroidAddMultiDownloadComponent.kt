package com.abdownloadmanager.android.pages.add.multiple

import com.abdownloadmanager.shared.action.createNewQueueAction
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pagemanager.NewQueuePageManager
import com.abdownloadmanager.shared.pages.adddownload.multiple.BaseAddMultiDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.multiple.OnRequestAdd
import com.abdownloadmanager.shared.pages.category.CategoryComponent
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.subscribeAsStateFlow
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer

class AndroidAddMultiDownloadComponent(
    ctx: ComponentContext,
    id: String,
    onRequestClose: () -> Unit,
    onRequestAdd: OnRequestAdd,
    lastSavedLocationsStorage: ILastSavedLocationsStorage,
    perHostSettingsManager: PerHostSettingsManager, downloadSystem: DownloadSystem,
    fileIconProvider: FileIconProvider,
    appRepository: BaseAppRepository,
    downloaderInUiRegistry: DownloaderInUiRegistry,
    queueManager: QueueManager,
    categoryManager: CategoryManager,
) : BaseAddMultiDownloadComponent(
    ctx = ctx,
    id = id,
    lastSavedLocationsStorage = lastSavedLocationsStorage,
    onRequestAdd = onRequestAdd,
    onRequestClose = onRequestClose,
    perHostSettingsManager = perHostSettingsManager,
    downloadSystem = downloadSystem,
    appRepository = appRepository,
    fileIconProvider = fileIconProvider,
    downloaderInUiRegistry = downloaderInUiRegistry,
    queueManager = queueManager,
    categoryManager = categoryManager,
), NewQueuePageManager, CategoryDialogManager {
    val categoryComponentNavigation = SlotNavigation<Long>()
    val categorySlot = childSlot(
        source = categoryComponentNavigation,
        childFactory = { config, ctx ->
            CategoryComponent(
                ctx = ctx,
                id = config,
                close = ::closeCategoryDialog,
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
                    closeCategoryDialog()
                },
            )
        },
        serializer = Long.serializer(),
    ).subscribeAsStateFlow()
    val newQueueAction = createNewQueueAction(
        scope,
        this,
    )

    override fun openCategoryDialog(categoryId: Long) {
        scope.launch {
            categoryComponentNavigation.activate(categoryId)
        }
    }

    override fun closeCategoryDialog() {
        scope.launch {
            categoryComponentNavigation.dismiss()
        }
    }

    override fun getCategoryPageManager(): CategoryDialogManager {
        return this
    }

    private val _showMoreInputs = MutableStateFlow(false)
    val showMoreOptions = _showMoreInputs.asStateFlow()
    fun setShowMoreOptions(value: Boolean) {
        _showMoreInputs.value = value
    }

    private val _showAddQueue = MutableStateFlow(false)
    val showAddQueue = _showAddQueue.asStateFlow()
    fun setShowAddQueue(value: Boolean) {
        _showAddQueue.value = value
    }

    fun createQueueWithName(name: String) {
        scope.launch { queueManager.addQueue(name) }
        setShowAddQueue(false)
    }

    override fun closeNewQueueDialog() {
        setShowAddQueue(false)
    }

    override fun openNewQueueDialog() {
        setShowAddQueue(true)
    }
}

