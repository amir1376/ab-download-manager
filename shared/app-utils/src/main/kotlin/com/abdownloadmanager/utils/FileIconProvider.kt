package com.abdownloadmanager.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.abdownloadmanager.utils.category.CategoryIconType
import com.abdownloadmanager.utils.category.CategoryManager
import com.abdownloadmanager.utils.category.DefaultCategories

interface FileIconProvider {

    /**
     * Automatically update icon if other dependencies changed
     */
    @Composable
    fun rememberCategoryIcon(fileName: String): CategoryIconType
}

class FileIconProviderUsingCategoryIcons(
    private val defaultCategories: DefaultCategories,
    private val categoryManager: CategoryManager,
) : FileIconProvider {

    @Composable
    override fun rememberCategoryIcon(fileName: String): CategoryIconType {
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
        return CategoryIconType.Other
    }

    private fun fromDefaultCategories(fileName: String): CategoryIconType? {
        return defaultCategories
            .getCategoryOfFileName(fileName)?.iconType
    }

    private fun fromUserDefinedCategories(fileName: String): CategoryIconType? {
        return categoryManager
            .getCategoryOfFileName(fileName)?.iconType
    }
}