package com.abdownloadmanager.desktop.pages.home.sections

import DOUBLE_CLICK_DELAY
import com.abdownloadmanager.desktop.ui.WithContentAlpha
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.widget.CheckBox
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.ui.widget.customtable.Table
import com.abdownloadmanager.desktop.ui.widget.customtable.*
import com.abdownloadmanager.desktop.ui.widget.customtable.styled.MyStyledTableHeader
import com.abdownloadmanager.desktop.ui.widget.menu.LocalMenuDisabledItemBehavior
import com.abdownloadmanager.desktop.ui.widget.menu.MenuDisabledItemBehavior
import com.abdownloadmanager.desktop.ui.widget.menu.ShowOptionsInDropDown
import com.abdownloadmanager.desktop.utils.action.MenuItem
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.remainingOrNull
import ir.amirab.downloader.monitor.speedOrNull
import ir.amirab.downloader.monitor.statusOrFinished
import kotlinx.coroutines.delay


class DownloadListContext(
    val onNewSelection: (List<Long>) -> Unit,
    val downloadList: List<IDownloadItemState>,
    val isAllSelected: Boolean,
) {
    fun newSelection(ids: List<Long>, isSelected: Boolean) {
        onNewSelection(ids.filter { isSelected })
    }

    fun changeAllSelection(isSelected: Boolean) {
        newSelection(downloadList.map { it.id }, isSelected)
    }
}

private val LocalDownloadListContext = compositionLocalOf<DownloadListContext> {
    error("DownloadListContext not provided")
}

@Composable
fun DownloadList(
    modifier: Modifier,
    downloadList: List<IDownloadItemState>,
    downloadOptions: MenuItem.SubMenu?,
    onRequestOpenOption: (IDownloadItemState) -> Unit,
    tableState: TableState<IDownloadItemState, DownloadListCells>,
    onRequestCloseOption: () -> Unit,
    selectionList: List<Long>,
    onItemSelectionChange: (Long, Boolean) -> Unit,
    onRequestOpenDownload: (Long) -> Unit,
    onNewSelection: (List<Long>) -> Unit,
    lastSelectedId: Long?,
) {
    val state = rememberLazyListState()
    ShowDownloadOptions(
        downloadOptions, onRequestCloseOption
    )
    val isALlSelected by derivedStateOf {
        val list = downloadList
        if (list.isEmpty()) {
            false
        } else {
            list.map { it.id }.all {
                it in selectionList
            }
        }

    }
    val tableInteractionSource = remember { MutableInteractionSource() }

    fun newSelection(ids: List<Long>, isSelected: Boolean) {
        onNewSelection(ids.filter { isSelected })
    }

    fun changeAllSelection(isSelected: Boolean) {
        newSelection(downloadList.map { it.id }, isSelected)
    }

    var isCtrlPressed by remember { mutableStateOf(false) }
    var isShiftPressed by remember { mutableStateOf(false) }
    CompositionLocalProvider(
        LocalDownloadListContext provides DownloadListContext(
            onNewSelection,
            downloadList,
            isALlSelected,
        )
    ) {
        val itemHorizontalPadding = 16.dp
        Table(
            tableState = tableState,
            state = state,
            key = { it.id },
            list = downloadList,
            modifier = modifier
                .onKeyEvent {
                    val ctrlPressed = it.isCtrlPressed
                    val shiftPressed = it.isShiftPressed
                    isCtrlPressed = ctrlPressed
                    isShiftPressed = shiftPressed
                    false
                }
                .onKeyEvent {
                    if (isCtrlPressed && it.key == Key.A) {
                        changeAllSelection(true)
                        true
                    } else {
                        false
                    }
                }
                .onKeyEvent {
                    if (it.key == Key.Escape) {
                        changeAllSelection(false)
                        true
                    } else {
                        false
                    }
                }
                .clickable(
                    indication = null,
                    interactionSource = tableInteractionSource,
                    onClick = {
                        //deselect all on click empty area
                        changeAllSelection(false)
                    },
                ),
            drawOnEmpty = {
                WithContentAlpha(0.75f) {
                    Text("List is empty.", Modifier.align(Alignment.Center))
                }
            },
            wrapHeader = {
                MyStyledTableHeader(itemHorizontalPadding = itemHorizontalPadding, content = it)
            },
            wrapItem = { item, rowContent ->
                val isSelected = selectionList.contains(item.id)
                var shouldWaitForSecondClick by remember {
                    mutableStateOf(false)
                }
                LaunchedEffect(shouldWaitForSecondClick) {
                    delay(DOUBLE_CLICK_DELAY)
                    if (shouldWaitForSecondClick) {
                        shouldWaitForSecondClick = false
                    }
                }
                val itemInteractionSource = remember { MutableInteractionSource() }
                CompositionLocalProvider(
                    LocalDownloadItemProperties provides DownloadItemProperties(
                        isSelected,
                        item,
                    )
                ) {
                    WithContentAlpha(1f) {
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            Modifier
                                .widthIn(min = getTableSize().visibleWidth)
                                .onClick(
                                    interactionSource = itemInteractionSource
                                ) {
                                    if (shouldWaitForSecondClick) {
                                        onRequestOpenDownload(item.id)
                                        shouldWaitForSecondClick = false
                                    } else {
                                        if (isCtrlPressed) {
                                            onItemSelectionChange(item.id, !isSelected)
                                        } else {
                                            changeAllSelection(false)
                                            onItemSelectionChange(item.id, true)
                                            shouldWaitForSecondClick = true
                                        }
                                    }
                                }
                                .onClick(
                                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                                ) {
                                    onRequestOpenOption(item)
                                }
                                .onClick(
                                    enabled = lastSelectedId != null,
                                    keyboardModifiers = {
                                        this.isShiftPressed
                                    }
                                ) {

                                    val lastSelected = lastSelectedId ?: return@onClick
                                    val currentId = item.id

                                    val ids = tableState.getARangeOfItems(
                                        list = downloadList,
                                        id = { it.id },
                                        fromItem = lastSelected,
                                        toItem = currentId,
                                    )
                                    newSelection(ids, true)
                                }
                                .padding(vertical = 1.dp)
                                .clip(shape)
                                .indication(
                                    interactionSource = itemInteractionSource,
                                    indication = LocalIndication.current
                                )
                                .hoverable(itemInteractionSource)
                                .let {
                                    if (isSelected) {
                                        val selectionColor = myColors.onBackground
                                        it
                                            .border(
                                                1.dp,
                                                myColors.selectionGradient(0.10f, 0.05f, selectionColor),
                                                shape
                                            )
                                            .background(myColors.selectionGradient(0.15f, 0f, selectionColor))
                                    } else {
                                        it.border(1.dp, Color.Transparent)
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = itemHorizontalPadding)
                        ) {
                            rowContent()
                        }
                    }
                }
            }
        ) { cell, item ->
            when (cell) {
                DownloadListCells.Check -> {
                    CheckCell(
                        onCheckedChange = { downloadId, isChecked ->
                            val currentSelection = selectionList.find {
                                downloadId == it
                            }?.let { true } ?: false
                            onItemSelectionChange(downloadId, !currentSelection)
                        },
                        dItemState = item
                    )
                }

                DownloadListCells.Name -> {
                    NameCell(item)
                }

                DownloadListCells.DateAdded -> {
                    DateAddedCell(item)
                }

                DownloadListCells.Size -> {
                    SizeCell(item)
                }

                DownloadListCells.Speed -> {
                    SpeedCell(item)
                }

                DownloadListCells.Status -> {
                    StatusCell(item)
                }

                DownloadListCells.TimeLeft -> {
                    TimeLeftCell(item)
                }
            }
        }
    }
}

sealed interface DownloadListCells : TableCell<IDownloadItemState> {
    data object Check : DownloadListCells,
        CustomCellRenderer {
        override val name: String = "#"
        override val size: CellSize = CellSize.Fixed(26.dp)

        @Composable
        override fun drawHeader() {
            val c = LocalDownloadListContext.current
            CheckBox(
                c.isAllSelected,
                {
                    c.changeAllSelection(it)
                },
                modifier = Modifier.size(12.dp)
            )
        }
    }

    data object Name : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.name

        override val name: String = "Name"
        override val size: CellSize = CellSize.Resizeable(50.dp..1000.dp, 200.dp)
    }

    data object Status : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.statusOrFinished().order

        override val name: String = "Status"
        override val size: CellSize = CellSize.Resizeable(100.dp..140.dp, 120.dp)
    }

    data object Size : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.contentLength

        override val name: String = "Size"
        override val size: CellSize = CellSize.Resizeable(70.dp..110.dp, 70.dp)
    }

    data object Speed : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.speedOrNull() ?: 0L

        override val name: String = "Speed"
        override val size: CellSize = CellSize.Resizeable(70.dp..110.dp, 80.dp)
    }

    data object TimeLeft : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.remainingOrNull() ?: Long.MAX_VALUE

        override val name: String = "Time Left"
        override val size: CellSize = CellSize.Resizeable(70.dp..150.dp, 100.dp)
    }

    data object DateAdded : DownloadListCells,
        SortableCell<IDownloadItemState> {
        override fun sortBy(item: IDownloadItemState): Comparable<*> = item.dateAdded

        override val name: String = "Date Added"
        override val size: CellSize = CellSize.Resizeable(90.dp..150.dp, 100.dp)
    }
}

@Composable
fun ShowDownloadOptions(
    options: MenuItem.SubMenu?,
    onDismiss: () -> Unit,
) {
    if (options != null) {
        CompositionLocalProvider(
            LocalMenuDisabledItemBehavior provides MenuDisabledItemBehavior.LowerOpacity
        ) {
            ShowOptionsInDropDown(options, onDismiss)
        }
    }

}

