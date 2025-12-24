package com.abdownloadmanager.android.pages.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.myColors
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen


@Composable
fun DownloadList(
    downloadList: List<IDownloadItemState>,
    selectionList: List<Long>,
    onItemSelectionChange: (Long, Boolean) -> Unit,
    onItemClicked: (IDownloadItemState) -> Unit,
    fileIconProvider: FileIconProvider,
    onNewSelection: (List<Long>) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {

    fun newSelection(ids: List<Long>, isSelected: Boolean) {
        onNewSelection(ids.filter { isSelected })
    }

    fun changeAllSelection(isSelected: Boolean) {
        newSelection(downloadList.map { it.id }, isSelected)
    }

    val isInSelectMode = selectionList.isNotEmpty()
    BackHandler(
        isInSelectMode
    ) {
        changeAllSelection(false)
    }
    val dividerColor = myColors.onBackground / 0.5f
    Box {
        LazyColumn(
            state = lazyListState,
            modifier = modifier,
            contentPadding = contentPadding
        ) {
            itemsIndexed(
                items = downloadList,
                key = { _, item -> item.id }
            ) { index, item ->
                val isFirstItem = index == 0
                Column(
                    modifier = Modifier.animateItem()
                ) {
                    RenderDownloadItem(
                        downloadItem = item,
                        checked = if (isInSelectMode) {
                            item.id in selectionList
                        } else {
                            null
                        },
                        onClick = {
                            if (isInSelectMode) {
                                val wasInSelections = item.id in selectionList
                                onItemSelectionChange(item.id, !wasInSelections)
                            } else {
                                onItemClicked(item)
                            }
                        },
                        onLongClick = {
                            val wasInSelections = item.id in selectionList
                            onItemSelectionChange(item.id, !wasInSelections)
                        },
                        fileIconProvider = fileIconProvider,
                        modifier = Modifier.ifThen(!isFirstItem) {
                            drawBehind {
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            dividerColor,
                                            Color.Transparent,
                                        )
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, 0f)
                                )
                            }
                        },
                    )
                }
            }
        }
        if (downloadList.isEmpty()) {
            Box(
                Modifier
                    .padding()
                    .fillMaxSize()
            ) {
                WithContentAlpha(0.75f) {
                    Text(
                        myStringResource(Res.string.list_is_empty),
                        Modifier.align(Alignment.Center),
                        maxLines = 1,
                    )
                }
            }
        }
    }

}
