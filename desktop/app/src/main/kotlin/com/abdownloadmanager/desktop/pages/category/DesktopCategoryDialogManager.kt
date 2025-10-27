package com.abdownloadmanager.desktop.pages.category

import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pages.category.CategoryComponent
import kotlinx.coroutines.flow.StateFlow

interface DesktopCategoryDialogManager : CategoryDialogManager {
    val openedCategoryDialogs: StateFlow<List<CategoryComponent>>
    fun closeCategoryDialog(categoryId: Long)
}
