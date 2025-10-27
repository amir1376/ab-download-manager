package com.abdownloadmanager.shared.ui.widget.sort

interface ComparatorProvider<T> {
    fun comparator(): Comparator<T>
}
