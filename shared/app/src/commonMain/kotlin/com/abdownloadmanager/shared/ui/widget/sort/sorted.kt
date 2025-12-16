package com.abdownloadmanager.shared.ui.widget.sort

import ir.amirab.util.ifThen

fun <ITEM, TComparatorProvider : ComparatorProvider<ITEM>> Sort<TComparatorProvider>.sorted(
    list: List<ITEM>
): List<ITEM> {
    return list
        .sortedWith(
            cell
                .comparator()
                .ifThen(isDescending()) { reversed() }
        )
}

fun <T : Sort<*>> T.toSortIndicatorMode(): SortIndicatorMode {
    return if (isDescending()) {
        SortIndicatorMode.Descending
    } else {
        SortIndicatorMode.Ascending
    }
}
