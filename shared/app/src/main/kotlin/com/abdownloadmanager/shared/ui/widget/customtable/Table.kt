package com.abdownloadmanager.shared.ui.widget.customtable

import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.menu.custom.MenuColumn
import com.abdownloadmanager.shared.utils.div
import ir.amirab.util.flow.saved
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.ui.theme.myShapes
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.shifted
import kotlinx.coroutines.flow.*
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableListItemScope

val LocalCellPadding = compositionLocalOf {
    PaddingValues(horizontal = 4.dp, vertical = 0.dp)
}
val LocalTableSize = compositionLocalOf<TableSize> {
    error("LocalTableConstraints not provided")
}
val LocalResizeCellsOnResizeTable = compositionLocalOf<Boolean> {
    error("LocalResizeCellsOnResizeTable not provided")
}

@Composable
fun <T, C : TableCell<T>> Table(
    list: List<T>,
    key: ((T) -> Any)? = null,
    tableState: TableState<T, C>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    resizeCellsOnResizeTableWidth: Boolean = false,
    renderHeaderCell: @Composable (C) -> Unit = { DefaultRenderHeader(it) },
    drawOnEmpty: @Composable BoxScope.() -> Unit = {},
    wrapHeader: @Composable TableScope.(rowContent: @Composable () -> Unit) -> Unit = { content -> content() },
    wrapItem: @Composable TableScope.(index: Int, item: T, rowContent: @Composable () -> Unit) -> Unit = { _, _, content -> content() },
    renderCell: @Composable TableScope.(C, T) -> Unit,
) {
    val scope = TableScope

    val visibleCells by tableState.visibleCells.collectAsState()
    val cellOrder by tableState.order.collectAsState()

    val cells = remember(visibleCells, cellOrder) {
        cellOrder.filter {
            it in visibleCells
        }
    }


    val sortedBy by tableState.sortBy.collectAsState()
    val customWidths by tableState.customSizes.collectAsState()
    TwoDimensionScrollbar(
        modifier = modifier,
        content = {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalTableSize provides TableSize(
                        visibleWidth = maxWidth,
                        visibleHeight = maxWidth,
                    ),
                    LocalResizeCellsOnResizeTable provides resizeCellsOnResizeTableWidth
                ) {
                    var showColumnConfig by remember {
                        mutableStateOf(false)
                    }
                    if (showColumnConfig) {
                        ShowColumnConfigMenu(
                            onDismissRequest = { showColumnConfig = false },
                            tableState = tableState
                        )
                    }
                    Column(
                        modifier = Modifier
                            .horizontalScroll(horizontalScrollState),
                    ) {
                        scope.wrapHeader {
                            Row(
                                Modifier
                                    .onClick(
                                        matcher = {
                                            it.button == PointerButton.Secondary
                                        }
                                    ) { showColumnConfig = true }
                                    .height(IntrinsicSize.Max),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                cells.forEach { cell ->
                                    val delta = scope.deltaWidthFraction(cell.size)
                                    val shouldResizeWidthOnResizeTable = scope.getIsResizeCellOnResizeTable()
                                    LaunchedEffect(cell.size, delta) {
                                        if (shouldResizeWidthOnResizeTable && cell.size is CellSize.Resizeable) {
                                            tableState.onCellSizeChanged(cell) { it * delta }
                                        }
                                    }
                                    Row(
                                        Modifier
                                            .width(customWidths[cell] ?: cell.size.defaultWidth)
//                                    .border(width = LocalCellPadding.current.calculateTopPadding(), myColors.primary,)
                                            .padding(LocalCellPadding.current),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        MaybeResizeableCell(
                                            cell,
                                            onResizeCell = {
                                                tableState.onCellSizeChanged(cell, it)
                                            }
                                        ) {
                                            MaybeSortableCell(
                                                cell,
                                                sortedBy,
                                                {
                                                    @Suppress("UNCHECKED_CAST")
                                                    tableState.setSortBy(Sort(cell as SortableCell<T>, it))
                                                }
                                            ) {
                                                Box(Modifier.weight(1f)) {
                                                    renderHeaderCell(cell)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val sortedList = remember(list, sortedBy) {
                            tableState.sortedList(list, sortedBy)
                        }
                        LazyColumn(
                            Modifier
                                .fillMaxHeight(),
                            state = listState,
                        ) {
                            itemsIndexed(
                                sortedList,
                                key = if (key != null) { _, item -> key(item) } else null
                            ) { index, item ->
                                scope.wrapItem(index, item) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        cells.forEach { cell ->
                                            Box(
                                                Modifier.width(customWidths[cell] ?: cell.size.defaultWidth)
                                                    .padding(LocalCellPadding.current)
                                            ) {
                                                scope.renderCell(cell, item)
                                            }
                                        }
                                    }
                                }
                            }
                        }
//        Spacer(Modifier.size(4.dp))
                    }
                    if (list.isEmpty()) {
                        Box(Modifier.padding().fillMaxSize()) {
                            drawOnEmpty()
                        }
                    }
                }
            }
        },
        horizontalAdapter = rememberScrollbarAdapter(horizontalScrollState),
        verticalAdapter = rememberScrollbarAdapter(listState),
    )

}

private fun androidx.compose.foundation.v2.ScrollbarAdapter.needScroll(): Boolean {
    return contentSize > viewportSize
}

@Composable
private fun TwoDimensionScrollbar(
    modifier: Modifier,
    content: @Composable () -> Unit,
    verticalAdapter: androidx.compose.foundation.v2.ScrollbarAdapter,
    horizontalAdapter: androidx.compose.foundation.v2.ScrollbarAdapter
) {
    Row(modifier) {
        Column(Modifier.weight(1f)) {
            Box(Modifier.weight(1f)) {
                content()
            }
            if (horizontalAdapter.needScroll()) {
                HorizontalScrollbar(
                    horizontalAdapter,
                    Modifier.padding(
                        top = 4.dp,
                        bottom = 4.dp,
                    )
                )
            }
        }
        if (verticalAdapter.needScroll()) {
            VerticalScrollbar(
                verticalAdapter,
                Modifier.padding(
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
            )
        }
    }
}


@Composable
private fun <T, C : TableCell<T>> ShowColumnConfigMenu(
    onDismissRequest: () -> Unit,
    tableState: TableState<T, C>,
) {
    Popup(
        popupPositionProvider = rememberCursorPositionProvider(
            alignment = Alignment.BottomEnd
        ),
        onDismissRequest = onDismissRequest
    ) {
        val visibleItems by tableState.visibleCells.collectAsState()
        val forceVisibleItems = tableState.forceVisibleCells
        MenuColumn {
            Row(
                Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyIcon(MyIcons.settings, null, Modifier.size(12.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    myStringResource(Res.string.customize_columns),
                    fontSize = myTextSizes.base
                )
            }
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.onSurface / 5))
            val orderedCells by tableState.order.collectAsState()
            ReorderableColumn(
                list = orderedCells,
                onSettle = { from, to ->
                    tableState.setOrder {
                        it.shifted(from, to - from)
                    }
                },
                content = { _, cell, _ ->
                    key(cell) {
                        ReorderableItem {
                            CellConfigItem(
                                modifier = Modifier.fillMaxWidth(),
                                cell = cell,
                                isVisible = cell in visibleItems,
                                isForceVisible = cell in forceVisibleItems,
                                setVisible = { checked ->
                                    tableState.setVisibleCells {
                                        val contains = it.contains(cell)
                                        if (checked) {
                                            it.ifThen(!contains) { plus(cell) }
                                        } else {
                                            it.ifThen(contains) { minus(cell) }
                                        }
                                    }
                                },
                                setSort = { sort ->
                                    tableState.setSortBy(sort)
                                },
                                sortBy = tableState.sortBy.collectAsState().value
                            )
                        }
                    }
                },
            )
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.onSurface / 5))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = tableState::reset)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                MyIcon(MyIcons.undo, null, Modifier.size(12.dp))
                Spacer(Modifier.size(8.dp))
                Text(myStringResource(Res.string.reset))
            }
        }
    }
}

@Composable
private fun <T, Cell : TableCell<T>> ReorderableListItemScope.CellConfigItem(
    modifier: Modifier,
    cell: Cell,
    isVisible: Boolean,
    isForceVisible: Boolean,
    setVisible: (Boolean) -> Unit,
    sortBy: Sort<SortableCell<T>>?,
    setSort: (Sort<SortableCell<T>>?) -> Unit,
) {
    Row(
        modifier
            .padding(8.dp)
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyIcon(
            icon = MyIcons.grip,
            contentDescription = null,
            modifier = Modifier
                .clip(myShapes.defaultRounded)
                .clickable {}
                .draggableHandle()
                .padding(4.dp)
                .size(12.dp),
        )
        Spacer(Modifier.width(8.dp))
        CheckBox(
            value = isVisible,
            enabled = !isForceVisible,
            onValueChange = {
                setVisible(it)
            },
            size = 12.dp
        )
        Spacer(Modifier.width(8.dp))
        Text(
            cell.name.rememberString(),
            Modifier
                .weight(1f)
                .ifThen(!isVisible || isForceVisible) {
                    alpha(0.5f)
                },
        )
        Spacer(Modifier.width(8.dp))
        if (cell is SortableCell<*>) {
            SortIndicator(
                Modifier.fillMaxHeight()
                    .clickable {
                        @Suppress("UNCHECKED_CAST")
                        setSort(sortBy?.takeIf { it.cell == cell }?.reverse() ?: Sort(cell as SortableCell<T>, true))
                    }.padding(horizontal = 2.dp)
                    .wrapContentHeight(),
                sortBy
                    ?.takeIf { it.cell == cell }
                    ?.let {
                        if (it.isUp()) {
                            SortIndicatorMode.Descending
                        } else {
                            SortIndicatorMode.Ascending
                        }
                    } ?: SortIndicatorMode.None
            )
        }
    }
}

interface TableScope {
    companion object : TableScope

    @Composable
    fun getTableSize() = LocalTableSize.current

    @Composable
    fun getIsResizeCellOnResizeTable() = LocalResizeCellsOnResizeTable.current

    @Composable
    fun lastWidths(key: Any): Pair<Dp, Dp> {
        val tableSize by rememberUpdatedState(getTableSize())
        var result by remember(key) {
            mutableStateOf(tableSize.visibleWidth to tableSize.visibleWidth)
        }
        LaunchedEffect(key) {
            snapshotFlow { tableSize.visibleWidth }
                .saved(2)
                .onEach {
                    when (it.size) {
                        0 -> null
                        1 -> {
                            result.copy(second = it.first())
                        }

                        else -> {
                            it[0] to it[1]
                        }
                    }?.let {
                        result = it
                    }
                }
                .launchIn(this)
        }
        return result
    }

    @Composable
    fun deltaWidthFraction(key: Any): Float {
        return lastWidths(key).run { second / first }
    }

    @Composable
    fun deltaWidth(key: Any): Dp {
        return lastWidths(key).run { second - first }
    }
}

@Composable
fun SortIndicator(
    modifier: Modifier = Modifier,
    mode: SortIndicatorMode,
) {
    val size = 6.dp
    Column(modifier) {
//        val currentAlpha = LocalContentAlpha.current
        val color = LocalContentColor.current
        val passiveAlpha = color / 0.25f
        val activeAlpha = color / 0.75f
//        val activeAlpha=(currentAlpha + 0.5f).coerceAtMost(1f)
        MyIcon(
            MyIcons.sortUp,
            null,
            Modifier
                .size(size),
            tint = if (mode.isAscending()) {
                activeAlpha
            } else {
                passiveAlpha
            }
        )
        MyIcon(
            MyIcons.sortDown,
            null,
            Modifier
                .size(size),
            tint = if (mode.isDescending()) {
                activeAlpha
            } else {
                passiveAlpha
            }
        )
    }
}
