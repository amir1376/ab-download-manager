package com.abdownloadmanager.shared.pages.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abdownloadmanager.shared.pages.home.category.DefinedStatusCategories
import com.abdownloadmanager.shared.pages.home.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.shared.util.category.Category
import ir.amirab.downloader.db.QueueModel

@Stable
class FilterState {
    var textToSearch by mutableStateOf("")
    var typeCategoryFilter by mutableStateOf(null as Category?)
    var queueFilter by mutableStateOf(null as QueueModel?)
    var statusFilter by mutableStateOf<DownloadStatusCategoryFilter>(DefinedStatusCategories.All)
}
