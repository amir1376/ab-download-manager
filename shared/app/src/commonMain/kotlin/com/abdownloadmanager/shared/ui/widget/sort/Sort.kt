package com.abdownloadmanager.shared.ui.widget.sort

import kotlinx.serialization.Serializable

@Serializable
data class Sort<Cell : ComparatorProvider<*>>(
    val cell: Cell,
    private val isDescending: Boolean,
) {
    fun isAscending() = !isDescending
    fun isDescending() = isDescending
    fun reverse(): Sort<Cell> {
        return copy(isDescending = !isDescending)
    }

    companion object {
        const val DEFAULT_IS_DESCENDING: Boolean = true
    }
}

