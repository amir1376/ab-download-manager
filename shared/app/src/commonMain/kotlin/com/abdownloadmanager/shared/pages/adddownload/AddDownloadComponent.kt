package com.abdownloadmanager.shared.pages.adddownload

import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pages.adddownload.addToQueue.SelectQueueComponent
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ir.amirab.downloader.queue.QueueManager
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okio.Path.Companion.toPath

sealed interface FolderChangeResult {
    val isChanged: Boolean

    data class OfCategory(
        override val isChanged: Boolean,
        val category: Category
    ) : FolderChangeResult

    data class OfDefault(
        override val isChanged: Boolean,
    ) : FolderChangeResult

    fun categoryOrNull(): Category? {
        return (this as? OfCategory)?.category
    }

    companion object {
        fun default() = OfDefault(
            isChanged = false,
        )


        private fun samePath(
            a: String,
            b: String,
        ): Boolean {
            return a == b || runCatching {
                a.toPath() == b.toPath()
            }.getOrElse { false }
        }

        fun of(
            category: Category?,
            currentFolder: String,
            userSelectedFolder: String,
        ): FolderChangeResult {
            return of(
                category = category,
                changed = !samePath(currentFolder, userSelectedFolder),
            )
        }

        fun of(
            category: Category?,
            changed: Boolean,
        ): FolderChangeResult {
            return when (category) {
                null -> OfDefault(changed)
                else -> OfCategory(changed, category)
            }
        }
    }
}

abstract class AddDownloadComponent(
    ctx: ComponentContext,
    val id: String,
    lastSavedLocationsStorage: ILastSavedLocationsStorage,
    protected val queueManager: QueueManager,
    private val selectQueueStorage: ISelectQueueStorage,
    protected val appRepository: BaseAppRepository,
    protected val categoryManager: CategoryManager,
) : BaseComponent(ctx) {
    companion object {
        const val lastLocationsCacheSize = 4
    }

    private val _rememberFolderAsDefault = MutableStateFlow(false)
    val rememberFolderAsDefault: StateFlow<Boolean> = _rememberFolderAsDefault.asStateFlow()

    fun setRememberFolderAsDefault(value: Boolean) {
        _rememberFolderAsDefault.value = value
    }

    abstract val isFolderChangedResult: StateFlow<FolderChangeResult>

    fun persistFolder(folder: String, category: Category?) {
        if (category == null) {
            appRepository.saveLocation.value = folder
        } else {
            categoryManager.updateCategory(category.id) {
                it.copy(
                    path = folder,
                    usePath = true,
                )
            }
        }
    }

    abstract fun getCategoryPageManager(): CategoryDialogManager
    fun onRequestAddCategory() {
        getCategoryPageManager().openCategoryDialog(-1)
    }

    private var dialogUsed = false
    protected fun consumeDialog(block: () -> Unit) {
        if (dialogUsed) {
            return
        }
        block()
        dialogUsed = true
    }

    private val _lastUsedLocations = lastSavedLocationsStorage.lastUsedSaveLocations
    val lastUsedLocations: StateFlow<List<String>> = _lastUsedLocations.asStateFlow()
    fun addToLastUsedLocations(saveLocation: String) {
        _lastUsedLocations.update {
            buildList {
                add(saveLocation)
                addAll(it)
            }
                .distinct()
                .take(lastLocationsCacheSize)
        }
    }

    fun removeFromLastDownloadLocation(saveLocation: String) {
        _lastUsedLocations.update {
            it.filter { it != saveLocation }
        }
    }

    abstract fun onRequestAddToQueue(
        queueId: Long?,
        startQueue: Boolean,
    )

    val selectQueueComponent = SelectQueueComponent(
        ctx = childContext("showAddToQueueComponent"),
        queueManager = queueManager,
        selectQueueStorage = selectQueueStorage,
        onRequestAddToQueue = {
            onRequestAddToQueue(it.queue, it.startQueue)
        }
    )

    abstract val shouldShowWindow: StateFlow<Boolean>
}

