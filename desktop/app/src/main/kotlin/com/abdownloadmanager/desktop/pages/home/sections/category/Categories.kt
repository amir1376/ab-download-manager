package com.abdownloadmanager.desktop.pages.home.sections.category

import androidx.compose.animation.*
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ExpandableItem
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.category.toCategoryImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Folder
import com.abdownloadmanager.desktop.ui.icons.default.Up
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

class DownloadStatusCategoryFilterByList(
    name: StringSource,
    icon: ImageVector,
    val acceptedStatus: List<DownloadStatus>,
) : DownloadStatusCategoryFilter(name, icon) {
    override fun accept(iDownloadStatus: IDownloadItemState): Boolean {
        return iDownloadStatus
            .statusOrFinished()
            .asDownloadStatus() in acceptedStatus
    }
}

abstract class DownloadStatusCategoryFilter(
    val name: StringSource,
    val icon: ImageVector,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

object DefinedStatusCategories {
    fun values() = listOf(All, Finished, Unfinished)


    val All = object : DownloadStatusCategoryFilter(
        name = Res.string.all.asStringSource(),
        icon = AbIcons.Default.Folder,
    ) {
        override fun accept(iDownloadStatus: IDownloadItemState): Boolean = true
    }
    val Finished = DownloadStatusCategoryFilterByList(
        name = Res.string.finished.asStringSource(),
        icon = AbIcons.Default.Folder,
        acceptedStatus = listOf(DownloadStatus.Completed)
    )
    val Unfinished = DownloadStatusCategoryFilterByList(
        name = Res.string.Unfinished.asStringSource(),
        icon = AbIcons.Default.Folder,
        acceptedStatus = listOf(
            DownloadStatus.Error,
            DownloadStatus.Added,
            DownloadStatus.Paused,
            DownloadStatus.Downloading,
        )
    )
}


@Composable
private fun CategoryFilterItem(
    modifier: Modifier,
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .background(
                if (isSelected) {
                    myColors.onBackground / 0.05f
                } else Color.Transparent
            )
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 24.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                Icon(
                    imageVector = category.toCategoryImageVector() ?: AbIcons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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
        AnimatedVisibility(
            isSelected,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = scaleIn(),
            exit = scaleOut(),
        ) {
            Spacer(
                Modifier
                    .height(16.dp)
                    .width(3.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 12.dp,
                            topEnd = 12.dp,
                        )
                    )
                    .background(myColors.primary)
            )
        }
    }
}

@Composable
fun StatusFilterItem(
    isExpanded: Boolean,
    onRequestExpand: (Boolean) -> Unit,
    currentTypeCategoryFilter: Category?,
    currentStatusCategoryFilter: DownloadStatusCategoryFilter?,
    statusFilter: DownloadStatusCategoryFilter,
    categories: List<Category>,
    onFilterChange: (
        typeFilter: Category?,
    ) -> Unit,
    onRequestOpenOptionMenu: (Category?) -> Unit,
) {
    val isStatusSelected = currentStatusCategoryFilter == statusFilter
    val isSelected = isStatusSelected && currentTypeCategoryFilter == null
    ExpandableItem(
        modifier = Modifier
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
            ) {
                onRequestOpenOptionMenu(null)
            },
        isExpanded = isExpanded,
        header = {
            Box(
                Modifier
                    .height(IntrinsicSize.Max)
                    .background(
                        if (isSelected) {
                            myColors.onBackground / 0.05f
                        } else Color.Transparent
                    )
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            if (!isExpanded) {
                                onRequestExpand(true)
                            }
                            onFilterChange(null)
                        }
                    )
            ) {
                Row(
                    Modifier.padding(vertical = 4.dp)
                        .padding(start = 16.dp)
                        .padding(end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WithContentAlpha(if (isSelected) 1f else 0.75f) {
                        Icon(
                            imageVector = statusFilter.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            statusFilter.name.rememberString(),
                            Modifier.weight(1f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = myTextSizes.lg,
                            maxLines = 1,
                        )
                        Icon(
                            imageVector = AbIcons.Default.Up,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxHeight().wrapContentHeight()
                                .clip(CircleShape)
                                .size(24.dp)
                                .clickable {
                                    onRequestExpand(!isExpanded)
                                }
                                .padding(6.dp)
                                .width(16.dp)
                                .rotate(if (isExpanded) 180f else 0f))
                    }
                }
                AnimatedVisibility(
                    isSelected,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    Spacer(
                        Modifier
                            .height(16.dp)
                            .width(3.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 12.dp,
                                    topEnd = 12.dp,
                                )
                            )
                            .background(myColors.primary)
                    )
                }
            }
        },
        body = {
            Column(Modifier) {
                categories.forEach {
                    CategoryFilterItem(
                        modifier = Modifier.onClick(
                            matcher = PointerMatcher.mouse(PointerButton.Secondary),
                        ) {
                            onRequestOpenOptionMenu(it)
                        },
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
