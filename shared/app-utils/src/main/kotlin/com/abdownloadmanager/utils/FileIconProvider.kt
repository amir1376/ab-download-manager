package com.abdownloadmanager.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.abdownloadmanager.utils.category.CategoryManager
import com.abdownloadmanager.utils.category.DefaultCategories
import com.abdownloadmanager.utils.category.iconSource
import com.abdownloadmanager.utils.compose.IMyIcons
import ir.amirab.util.compose.IconSource


interface FileIconProvider {
    fun getIcon(fileName: String): IconSource

    /**
     * Automatically update icon if other dependencies changed
     */
    @Composable
    fun rememberIcon(fileName: String): IconSource
}

class FileIconProviderUsingCategoryIcons(
    private val defaultCategories: DefaultCategories,
    private val categoryManager: CategoryManager,
    private val icons: IMyIcons,
) : FileIconProvider {
    override fun getIcon(fileName: String): IconSource {
        return fromDefaultCategories(fileName)
            ?: fromUserDefinedCategories(fileName)
            ?: icons.file
    }

    @Composable
    override fun rememberIcon(fileName: String): IconSource {
        val fromDefault = remember(fileName) {
            fromDefaultCategories(fileName)
        }
        if (fromDefault != null) {
            return fromDefault
        }
        val categories by categoryManager.categoriesFlow.collectAsState()
        val fromCategories = remember(fileName, categories) {
            fromUserDefinedCategories(fileName)
        }
        if (fromCategories != null) {
            return fromCategories
        }
        return icons.file
    }

    private fun fromDefaultCategories(fileName: String): IconSource? {
        return defaultCategories
            .getCategoryOfFileName(fileName)?.iconSource()
    }

    private fun fromUserDefinedCategories(fileName: String): IconSource? {
        return categoryManager
            .getCategoryOfFileName(fileName)?.iconSource()
    }
}