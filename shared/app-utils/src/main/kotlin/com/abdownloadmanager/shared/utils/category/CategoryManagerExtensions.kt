package com.abdownloadmanager.shared.utils.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun CategoryManager.rememberCategoryOf(
    itemId: Long,
): Category? {
    val categories by categoriesFlow.collectAsState()
    return remember(itemId, categories) {
        categories.firstOrNull {
            it.items.contains(itemId)
        }
    }
}
