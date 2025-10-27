package com.abdownloadmanager.shared.util.category

import androidx.compose.runtime.Immutable

interface ICategoryItem {
    val fileName: String
    val url: String
}

@Immutable
data class CategoryItem(
    override val fileName: String,
    override val url: String,
) : ICategoryItem

@Immutable
data class CategoryItemWithId(
    val id: Long,
    override val fileName: String,
    override val url: String,
) : ICategoryItem
