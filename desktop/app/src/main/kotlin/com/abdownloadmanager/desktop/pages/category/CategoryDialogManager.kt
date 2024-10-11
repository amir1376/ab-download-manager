package com.abdownloadmanager.desktop.pages.category

import kotlinx.coroutines.flow.StateFlow

interface CategoryDialogManager {
    val openedCategoryDialogs: StateFlow<List<CategoryComponent>>
    fun openCategoryDialog(categoryId: Long)
    fun closeCategoryDialog(categoryId: Long)
}