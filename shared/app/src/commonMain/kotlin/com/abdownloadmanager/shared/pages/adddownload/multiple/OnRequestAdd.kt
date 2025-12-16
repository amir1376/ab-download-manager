package com.abdownloadmanager.shared.pages.adddownload.multiple

import com.abdownloadmanager.shared.util.category.CategorySelectionMode
import ir.amirab.downloader.NewDownloadItemProps
import kotlinx.coroutines.Deferred

fun interface OnRequestAdd {
    operator fun invoke(
        items: List<NewDownloadItemProps>,
        queueId: Long?,
        categorySelectionMode: CategorySelectionMode?,
    ): Deferred<List<Long>>
}
