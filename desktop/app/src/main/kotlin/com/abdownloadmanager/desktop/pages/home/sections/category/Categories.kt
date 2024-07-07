package com.abdownloadmanager.desktop.pages.home.sections.category

import com.abdownloadmanager.desktop.ui.icon.IconSource
import com.abdownloadmanager.desktop.ui.icon.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ExpandableItem
import com.abdownloadmanager.desktop.ui.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished

abstract class DownloadTypeCategoryFilter(
    val name: String,
    val icon: IconSource,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

class DownloadTypeCategoryFilterByList(
    name: String,
    icon: IconSource,
    acceptedTypes: List<String>,
) : DownloadTypeCategoryFilter(name,icon) {
    val acceptedTypes = acceptedTypes.map { it.lowercase() }
    override fun accept(iDownloadStatus: IDownloadItemState): Boolean {
        val extension = iDownloadStatus.name
            .split(".")
            .lastOrNull()
            ?.lowercase() ?: return false
        return extension in acceptedTypes
    }
}


class DownloadStatusCategoryFilterByList(
    name: String,
    icon: IconSource,
    val acceptedStatus: List<DownloadStatus>,
) : DownloadStatusCategoryFilter(name, icon) {
    override fun accept(iDownloadStatus: IDownloadItemState): Boolean {
        return iDownloadStatus
            .statusOrFinished()
            .asDownloadStatus() in acceptedStatus
    }
}

abstract class DownloadStatusCategoryFilter(
    val name: String,
    val icon: IconSource,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

object DefinedStatusCategories {
    fun values() = listOf(All, Finished, Unfinished)


    val All = object : DownloadStatusCategoryFilter(
        "All",
        MyIcons.folder,
    ) {
        override fun accept(iDownloadStatus: IDownloadItemState): Boolean = true
    }
    val Finished = DownloadStatusCategoryFilterByList(
        "Finished",
        MyIcons.folder,
        listOf(DownloadStatus.Completed)
    )
    val Unfinished = DownloadStatusCategoryFilterByList(
        "Unfinished",
        MyIcons.folder,
        listOf(
            DownloadStatus.Error,
            DownloadStatus.Added,
            DownloadStatus.Paused,
            DownloadStatus.Downloading,
        )
    )
}

object DefinedTypeCategories {
    fun values() = listOf(
        Image, Music, Video, App, Document, Compressed, Other
    )

    fun resolveCategoryForDownloadItem(item: IDownloadItemState): DownloadTypeCategoryFilter {
        return values().first {
            it.accept(item)
        }
    }


    val Image = DownloadTypeCategoryFilterByList(
        "Image",
        MyIcons.pictureFile,
        listOf("png", "jpg", "jpeg", "gif", "svg")
    )
    val Music = DownloadTypeCategoryFilterByList(
        "Music",
        MyIcons.musicFile,
        listOf("mp3")
    )
    val Video = DownloadTypeCategoryFilterByList(
        "Video",
        MyIcons.videoFile,
        listOf("mp4", "mkv", "3gp", "avi")
    )
    val App = DownloadTypeCategoryFilterByList(
        "Apps",
        MyIcons.applicationFile,
        listOf("apk", "deb", "exe", "msi", "jar")
    )
    val Document = DownloadTypeCategoryFilterByList(
        "Document",
        MyIcons.documentFile,
        listOf("txt", "docx", "pdf")
    )
    val Compressed = DownloadTypeCategoryFilterByList(
        "Compressed",
        MyIcons.zipFile,
        listOf("zip", "rar", "tz")
    )
    val Other = object : DownloadTypeCategoryFilter(
        "Other",
        MyIcons.otherFile,
    ) {
        override fun accept(iDownloadStatus: IDownloadItemState): Boolean =true
    }
}


@Composable
private fun CategoryFilterItem(
    modifier: Modifier,
    category: DownloadTypeCategoryFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(start = 24.dp)
            .padding(horizontal = 4.dp,vertical = 6.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WithContentAlpha(if (isSelected)1f else 0.75f){
            MyIcon(
                category.icon,
                null,
                Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                category.name,
                Modifier.weight(1f),
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = myTextSizes.base
            )
        }
    }
}

@Composable
fun StatusFilterItem(
    isExpanded: Boolean,
    onRequestExpand: (Boolean) -> Unit,
    currentTypeCategoryFilter: DownloadTypeCategoryFilter?,
    currentStatusCategoryFilter: DownloadStatusCategoryFilter?,
    statusFilter: DownloadStatusCategoryFilter,
    typeFilter: List<DownloadTypeCategoryFilter>,
    onFilterChange: (
        typeFilter: DownloadTypeCategoryFilter?,
    ) -> Unit,
) {
    val isStatusSelected = currentStatusCategoryFilter == statusFilter
    val isSelected = isStatusSelected && currentTypeCategoryFilter == null
    ExpandableItem(
        isExpanded = isExpanded,
        header = {
            Row(
                Modifier
                    .height(IntrinsicSize.Max)
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            if (!isExpanded) {
                                onRequestExpand(true)
                            }
                            onFilterChange(null)
                        }
                    ).padding(vertical = 4.dp)
                    .padding(start = 16.dp)
                    .padding(end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WithContentAlpha(if (isSelected) 1f else 0.75f) {
                    MyIcon(
                        statusFilter.icon,
                        null,
                        Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        statusFilter.name,
                        Modifier.weight(1f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = myTextSizes.lg,
                        maxLines = 1,
                    )
                    MyIcon(MyIcons.up, null, Modifier
                        .fillMaxHeight().wrapContentHeight()
                        .clip(CircleShape)
                        .size(24.dp)
                        .clickable {
                            onRequestExpand(!isExpanded)
                        }
                        .padding(6.dp)
                        .width(16.dp)
                        .let {
                            it.rotate(if (isExpanded) 180f else 0f)
                        })
                }
            }
        },
        body = {
            Column(Modifier) {
                typeFilter.forEach {
                    CategoryFilterItem(
                        modifier = Modifier,
                        category = it,
                        isSelected = isStatusSelected && currentTypeCategoryFilter == it,
                        onClick = {
                            onFilterChange(it)
                        }
                    )
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    )
}
