package com.abdownloadmanager.shared.ui.widget.customtable

enum class SortIndicatorMode {
    None,
    Ascending,
    Descending,
}

fun SortIndicatorMode.isAscending(): Boolean {
    return when (this) {
        SortIndicatorMode.Ascending -> true
        else -> false
    }
}

fun SortIndicatorMode.isDescending(): Boolean {
    return when (this) {
        SortIndicatorMode.Descending -> true
        else -> false
    }
}

fun SortIndicatorMode.next(): SortIndicatorMode {
    return when (this) {
        SortIndicatorMode.None -> SortIndicatorMode.Ascending
        SortIndicatorMode.Ascending -> SortIndicatorMode.Descending
        SortIndicatorMode.Descending -> SortIndicatorMode.Ascending
    }
}