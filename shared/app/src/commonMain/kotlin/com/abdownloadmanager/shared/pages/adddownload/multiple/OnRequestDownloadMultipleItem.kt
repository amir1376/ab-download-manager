package com.abdownloadmanager.shared.pages.adddownload.multiple

import com.abdownloadmanager.shared.util.category.CategorySelectionMode
import ir.amirab.downloader.NewDownloadItemProps
import kotlinx.coroutines.Deferred

fun interface OnRequestDownloadMultipleItem {
    operator fun invoke(
        items: List<NewDownloadItemProps>,
        categorySelectionMode: CategorySelectionMode?,
    ): Deferred<List<Long>>
}
