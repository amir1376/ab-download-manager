package com.abdownloadmanager.shared.pages.home

import androidx.compose.runtime.Stable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Stable
class CategoryActions(
    private val scope: CoroutineScope,
    private val categoryManager: CategoryManager,
    private val defaultCategories: DefaultCategories,

    val categoryItem: Category?,

    private val openFolder: (Category) -> Unit,
    private val requestDelete: (Category) -> Unit,
    private val requestEdit: (Category) -> Unit,

    private val onRequestResetToDefaults: () -> Unit,
    private val onRequestCategorizeItems: () -> Unit,
    private val onRequestAddCategory: () -> Unit,
) {
    private val mainItemExists = MutableStateFlow(categoryItem != null)
    private val canBeOpened = MutableStateFlow(categoryItem?.usePath ?: false)
    private inline fun useItem(
        block: (Category) -> Unit,
    ) {
        categoryItem?.let(block)
    }

    val openCategoryFolderAction = simpleAction(
        title = Res.string.open_folder.asStringSource(),
        icon = MyIcons.folderOpen,
        checkEnable = canBeOpened,
        onActionPerformed = {
            scope.launch {
                useItem {
                    openFolder(it)
                }
            }
        }
    )

    val deleteAction = simpleAction(
        title = Res.string.delete_category.asStringSource(),
        icon = MyIcons.remove,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestDelete(it)
                }
            }
        },
    )
    val editAction = simpleAction(
        title = Res.string.edit_category.asStringSource(),
        icon = MyIcons.settings,
        checkEnable = mainItemExists,
        onActionPerformed = {
            scope.launch {
                useItem {
                    requestEdit(it)
                }
            }
        },
    )

    val addCategoryAction = simpleAction(
        title = Res.string.add_category.asStringSource(),
        icon = MyIcons.add,
        onActionPerformed = {
            scope.launch {
                onRequestAddCategory()
            }
        },
    )
    val categorizeItemsAction = simpleAction(
        title = Res.string.auto_categorize_downloads.asStringSource(),
        icon = MyIcons.refresh,
        onActionPerformed = {
            scope.launch {
                onRequestCategorizeItems()
            }
        },
    )
    val resetToDefaultAction = simpleAction(
        title = Res.string.restore_defaults.asStringSource(),
        icon = MyIcons.undo,
        checkEnable = categoryManager
            .categoriesFlow
            .mapStateFlow { !defaultCategories.isDefault(it) },
        onActionPerformed = {
            scope.launch {
                onRequestResetToDefaults()
            }
        },
    )

    val menu: List<MenuItem> = buildMenu {
        +editAction
        +openCategoryFolderAction
        +deleteAction
        separator()
        +addCategoryAction
        separator()
        +categorizeItemsAction
        +resetToDefaultAction
    }
}
