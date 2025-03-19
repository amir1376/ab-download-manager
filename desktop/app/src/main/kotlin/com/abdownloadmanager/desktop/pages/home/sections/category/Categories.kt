package com.abdownloadmanager.desktop.pages.home.sections.category

import androidx.compose.animation.*
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.category.Category
import com.abdownloadmanager.shared.utils.category.rememberIconPainter
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

class DownloadStatusCategoryFilterByList(
    name: StringSource,
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
    val name: StringSource,
    val icon: IconSource,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

object DefinedStatusCategories {
    fun values() = listOf(All, Finished, Unfinished)


    val All = object : DownloadStatusCategoryFilter(
        Res.string.all.asStringSource(),
        MyIcons.folder,
    ) {
        override fun accept(iDownloadStatus: IDownloadItemState): Boolean = true
    }
    val Finished = DownloadStatusCategoryFilterByList(
        Res.string.finished.asStringSource(),
        MyIcons.folder,
        listOf(DownloadStatus.Completed)
    )
    val Unfinished = DownloadStatusCategoryFilterByList(
        Res.string.Unfinished.asStringSource(),
        MyIcons.folder,
        listOf(
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
                val iconPainter = category.rememberIconPainter()
                MyIcon(
                    iconPainter ?: MyIcons.folder,
                    null,
                    Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    category.name,
                    Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                        MyIcon(
                            statusFilter.icon,
                            null,
                            Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            statusFilter.name.rememberString(),
                            Modifier.weight(1f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = myTextSizes.lg,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        MyIcon(
                            MyIcons.up, null, Modifier
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
