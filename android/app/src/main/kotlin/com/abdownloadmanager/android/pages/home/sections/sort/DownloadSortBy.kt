package com.abdownloadmanager.android.pages.home.sections.sort

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.sort.ComparatorProvider
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DownloadSortBy(
    val selector: (IDownloadItemState) -> Comparable<*>,
    val icon: IconSource,
    val name: StringSource,
) : ComparatorProvider<IDownloadItemState> {
    override fun comparator(): Comparator<IDownloadItemState> {
        return compareBy(selector)
    }

    @Serializable
    @SerialName("name")
    object Name : DownloadSortBy(
        selector = { it.name },
        icon = MyIcons.alphabet,
        name = Res.string.name.asStringSource(),
    )

    @Serializable
    @SerialName("dateAdded")
    object DataAdded : DownloadSortBy(
        selector = { it.dateAdded },
        icon = MyIcons.clock,
        name = Res.string.date_added.asStringSource(),
    )

    @Serializable
    @SerialName("status")
    data object Status : DownloadSortBy(
        selector = { it.statusOrFinished().order },
        icon = MyIcons.info,
        name = Res.string.status.asStringSource(),
    )

    @Serializable
    @SerialName("size")
    data object Size : DownloadSortBy(
        selector = { it.contentLength },
        icon = MyIcons.data,
        name = Res.string.size.asStringSource(),
    )
}
