package com.abdownloadmanager.shared.util.category

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CategorySelectionMode {
    data class Fixed(val categoryId: Long) : CategorySelectionMode
    data object Auto : CategorySelectionMode
}
