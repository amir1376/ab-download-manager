package com.abdownloadmanager.utils.category

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File

class CategoryManager(
    private val categoryStorage: CategoryStorage,
    private val scope: CoroutineScope,
    private val defaultCategoriesFactory: DefaultCategories,
    private val categoryItemProvider: ICategoryItemProvider,
) {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categoriesFlow = _categories.asStateFlow()

    private var booted = false

    @OptIn(FlowPreview::class)
    suspend fun boot() {
        synchronized(this) {
            if (booted) return
        }
        if (categoryStorage.isCategoriesSet()) {
            _categories.value = categoryStorage
                .getCategories()
        } else {
            reset()
        }
        _categories
            .sample(500)
            .onEach { categoryStorage.setCategories(it) }
            .launchIn(scope)
        booted = true
    }

    suspend fun reset() {
        val newCategories = defaultCategoriesFactory.getDefaultCategories()
        setCategories(newCategories)
        withContext(Dispatchers.IO) {
            newCategories.forEach {
                prepareCategory(it)
            }
            autoAddItemsToCategoriesBasedOnFileNames(
                categoryItemProvider
                    .getAll()
            )
        }
    }

    fun getCategories(): List<Category> {
        return _categories.value
    }

    fun setCategories(categories: List<Category>) {
        _categories.update { categories }
    }

    fun getCategoryById(id: Long): Category? {
        return getCategories()
            .firstOrNull { it.id == id }
    }

    fun getCategoryOfType(extension: String): Category? {
        return getCategories().firstOrNull { c ->
            c.acceptedFileTypes.any {
                it.equals(extension, true)
            }
        }
    }

    fun getCategoryOfFileName(fileName: String): Category? {
        return getCategories()
            .firstOrNull {
                it.acceptFileName(fileName)
            }
    }

    fun getCategoryOf(categoryItem: ICategoryItem): Category? {
        val url = categoryItem.url
        val fileName = categoryItem.fileName
        return getCategories()
            .filter {
                it.acceptFileName(fileName)
            }.sortedByDescending {
                it.hasUrlPattern
            }.firstOrNull {
                it.acceptUrl(url)
            }

    }

    fun getCategoryOfItem(id: Long): Category? {
        return getCategories()
            .firstOrNull {
                it.items.contains(id)
            }
    }

    fun deleteCategory(category: Category) {
        deleteCategory(category.id)
    }

    fun deleteCategory(categoryId: Long) {
        _categories.update {
            it.filter {
                it.id != categoryId
            }
        }
    }

    fun addCustomCategory(category: Category) {
        require(category.id == -1L)
        val categories = getCategories()
        val newId = (
                categories
                    .maxOfOrNull { it.id }
                    ?.coerceAtLeast(DEFAULT_CATEGORY_END_ID)
                    ?: DEFAULT_CATEGORY_END_ID
                ) + 1
        val newCategory = category.copy(
            id = newId
        )
        setCategories(
            categories.plus(
                newCategory
            )
        )
        prepareCategory(newCategory)
    }

    private fun createDirectoryIfNecessary(category: Category) {
        kotlin.runCatching {
            val folder = category
                .getDownloadPath()
                ?.let(::File)
                ?.canonicalFile
                ?: return
            if (!folder.exists()) {
                folder.mkdirs()
            }
        }
    }

    private fun prepareCategory(newCategory: Category) {
        createDirectoryIfNecessary(newCategory)
    }

    fun updateCategory(categoryToUpdate: Category) {
        _categories.update {
            it.updatedItem(
                categoryId = categoryToUpdate.id,
                update = { categoryToUpdate }
            )
        }
    }

    fun updateCategory(id: Long, categoryToUpdate: (Category) -> Category) {
        _categories.update {
            it.updatedItem(id, categoryToUpdate)
        }
    }


    fun addItemsToCategory(categoryId: Long, itemIds: List<Long>) {
        _categories.update { previousCategories ->
            previousCategories
                .removedItemIds(itemIds)
                .updatedItem(categoryId) {
                    it.withExtraItems(itemIds)
                }
        }
    }

    fun removeItemInCategories(idsToRemove: List<Long>) {
        _categories.update {
            it.removedItemIds(idsToRemove)
        }
    }

    fun isDefaultCategory(category: Category): Boolean {
        return category.id in 0..DEFAULT_CATEGORY_END_ID
    }

    fun autoAddItemsToCategoriesBasedOnFileNames(
        unCategorizedItems: List<CategoryItemWithId>,
    ) {
        val newItemsMap = mutableMapOf<Long, MutableList<Long>>()
        var count = 0
        for (item in unCategorizedItems) {
            val categoryToUpdate = getCategoryOf(item) ?: continue
            newItemsMap
                .getOrPut(categoryToUpdate.id) { mutableListOf() }
                .add(item.id)
            count++
        }
        for ((categoryId, itemsToAdd) in newItemsMap) {
            updateCategory(categoryId) {
                it.withExtraItems(itemsToAdd)
            }
        }
    }

    fun isThisPathBelongsToACategory(folder: String): Boolean {
        return getCategories()
            .mapNotNull { it.getDownloadPath() }.contains(folder)
    }

    @Suppress("NAME_SHADOWING")
    fun updateCategoryFoldersBasedOnDefaultDownloadFolder(
        previousDownloadFolder: String,
        currentDownloadFolder: String,
    ) {
        val previousDownloadFolder = File(previousDownloadFolder).absoluteFile
        val currentDownloadFolder = File(currentDownloadFolder).absoluteFile
        for (category in getCategories()) {
            val categoryPath = File(category.path).absoluteFile
            if (categoryPath.startsWith(previousDownloadFolder)) {
                val relativePath = categoryPath.relativeTo(previousDownloadFolder)
                updateCategory(category.id) {
                    it.copy(
                        path = currentDownloadFolder.resolve(relativePath).absolutePath
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Reserved ids for default categories
         * this is too big BTW as we only use 5 for now
         * maybe we need more or extra hidden categories that users can enable (maybe ?)
         */
        const val DEFAULT_CATEGORY_END_ID = 100L
    }
}

private fun List<Category>.removedItemIds(itemIds: List<Long>): List<Category> {
    return map {
        it.copy(
            items = it.items.filter { itemId ->
                itemId !in itemIds
            }
        )
    }
}

private inline fun List<Category>.updatedItem(categoryId: Long, update: (Category) -> Category): List<Category> {
    return map {
        if (it.id == categoryId) {
            update(it)
        } else it
    }
}