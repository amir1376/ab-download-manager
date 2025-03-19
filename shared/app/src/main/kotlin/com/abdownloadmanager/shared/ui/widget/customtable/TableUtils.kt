package com.abdownloadmanager.shared.ui.widget.customtable

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.ui.widget.resizeHandle
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.amirab.util.compose.StringSource
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.swapped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import java.awt.Cursor

@Stable
data class TableSize(
    val visibleHeight: Dp,
    val visibleWidth: Dp,
)

@Immutable
sealed interface CellSize {
    val defaultWidth: Dp

    data class Resizeable(
        val range: ClosedRange<Dp>,
        override val defaultWidth: Dp = range.start,
    ) : CellSize

    data class Fixed(
        override val defaultWidth: Dp
    ) : CellSize
}

@Stable
interface TableCell<Item> {
    val id: String
    val name: StringSource
    val size: CellSize
}

interface CustomCellRenderer {
    @Composable
    fun drawHeader()
}

interface SortableCell<Item> : TableCell<Item> {
    fun comparator(): Comparator<Item>
}


data class Sort<Cell : SortableCell<*>>(
    val cell: Cell,
    private val isUp: Boolean,
) {
    fun isUp() = isUp
    fun isDown() = !isUp
    fun reverse(): Sort<Cell> {
        return copy(isUp = !isUp())
    }
}


@Composable
fun DefaultRenderHeader(cell: TableCell<*>) {
    if (cell is CustomCellRenderer) {
        cell.drawHeader()
    } else {
        Text(
            cell.name.rememberString(),
            Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RowScope.MaybeResizeableCell(
    cell: TableCell<*>,
    onResizeCell: ((Dp) -> Dp) -> Unit,
    content: @Composable () -> Unit,
) {
    when (cell.size) {
        is CellSize.Fixed -> {
            content()
        }

        is CellSize.Resizeable -> {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val mInteractionSource = remember {
                    MutableInteractionSource()
                }
                content()
                CellResizeHandle(
                    Modifier.width(12.dp)
                        .fillMaxHeight(),
                    orientation = Orientation.Horizontal,
                    mInteractionSource,
                    color = myColors.onBackground / 50,
                    inactiveColor = myColors.onBackground / 10,
                ) { delta ->
                    onResizeCell {
                        it + delta
                    }
                }
            }
        }
    }
}

@Composable
fun CellResizeHandle(
    modifier: Modifier,
    orientation: Orientation = Orientation.Horizontal,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = myColors.surface,
    inactiveColor: Color = myColors.surface / 50,
    onDrag: (Dp) -> Unit,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDragging by interactionSource.collectIsDraggedAsState()

    val hoverIcon = remember(orientation) {
        PointerIcon(
            Cursor(
                when (orientation) {
                    Orientation.Vertical -> Cursor.S_RESIZE_CURSOR
                    Orientation.Horizontal -> Cursor.E_RESIZE_CURSOR
                }
            )
        )
    }
    val background = animateColorAsState(
        if (isHovered || isDragging) color
        else inactiveColor
    ).value
    Box(modifier
        .pointerHoverIcon(hoverIcon, true)
        .hoverable(interactionSource)
        .resizeHandle(
            orientation = orientation,
            interactionSource = interactionSource,
            onDrag = onDrag,
        )
    ) {
        Row(Modifier
            .fillMaxSize().wrapContentSize()
        ) {
            val m = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(background)
            Spacer(m)
        }
    }
}

@Composable
fun <T> RowScope.MaybeSortableCell(
    cell: TableCell<T>,
    sortedBy: Sort<SortableCell<T>>?,
    setSort: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    if (cell is SortableCell) {
        val iHaveSorted = sortedBy.takeIf {
            it?.cell == cell
        }
        Row(
            Modifier
                .weight(1f)
                .onClick {
                    setSort(iHaveSorted?.reverse()?.isUp() ?: true)
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortIndicator(
                Modifier,
                iHaveSorted?.let {
                    if (it.isUp()) SortIndicatorMode.Descending
                    else SortIndicatorMode.Ascending
                } ?: SortIndicatorMode.None
            )
            Spacer(Modifier.width(2.dp))
            content()
        }
    } else {
        content()
    }
}

@Stable
class TableState<Item, Cell : TableCell<Item>>(
    val cells: List<Cell>,
    val forceVisibleCells: List<Cell> = emptyList(),
    val initialCustomSizes: Map<Cell, Dp> = emptyMap(),
    val initialSortBy: Sort<SortableCell<Item>>? = null,
    val initialOrder: List<Cell> = cells,
    val initialVisibleItems: List<Cell> = cells,
) {
    private val _customSizes = MutableStateFlow<Map<Cell, Dp>>(initialCustomSizes)
    val customSizes = _customSizes.asStateFlow()

    fun setCustomSizes(sizes: Map<Cell, Dp>){
        setCustomSizes { sizes }
    }
    fun setCustomSizes(sizes: (Map<Cell, Dp>)->Map<Cell, Dp>){
        _customSizes.update {
            sizes(it)
        }
    }

    fun onCellSizeChanged(cell: Cell, change: (Dp) -> Dp) {
        val customSizes = _customSizes.value
        val size = cell.size as? CellSize.Resizeable ?: run {
            error("can't resize this column because it have a FixedSize")
        }
        val dp = customSizes[cell]
        val x = change((dp ?: size.defaultWidth)).coerceIn(size.range)
        if (x == cell.size.defaultWidth) {
            setCustomSizes {
                it.minus(cell)
            }
        } else {
            setCustomSizes {
                customSizes.plus(cell to x)
            }
        }
    }

    private val _sortBy = MutableStateFlow<Sort<SortableCell<Item>>?>(initialSortBy)
    val sortBy = _sortBy.asStateFlow()
    fun setSortBy(cell: Sort<SortableCell<Item>>?) {
        this._sortBy.update { cell }
    }

    private val _order = MutableStateFlow(initialOrder)
    val order = _order
        .asStateFlow()
        .mapStateFlow {
            val remainingCells = cells.subtract(it.toSet())
            it.plus(remainingCells)
        }

    fun setOrder(updater: (List<Cell>) -> List<Cell>) {
        _order.update {
            updater(it)
        }
    }

    fun setOrder(cell: Cell, delta: Int) {
        setOrder {
            val index = it.indexOf(cell)
            val newIndex = (index + delta)
            val shouldMove = newIndex in it.indices
            if (shouldMove) {
                it.swapped(index, newIndex)
            } else it
        }
    }

    fun setOrder(order: List<Cell>) {
        setOrder { order }
    }

    private val _visibleCells = MutableStateFlow<List<Cell>>(initialVisibleItems)
    val visibleCells = _visibleCells.asStateFlow()
        .mapStateFlow {
            it.plus(forceVisibleCells.subtract(it.toSet()))
        }

    fun setVisibleCells(cells: (List<Cell>) -> List<Cell>) {
        _visibleCells.update {
            cells(it).distinct().toMutableList()
        }
    }

    fun setVisibleCells(cells: List<Cell>) {
        setVisibleCells { cells }
    }

    fun reset() {
        setCustomSizes(initialCustomSizes)
        setVisibleCells(initialVisibleItems)
        setOrder(initialOrder)
        setSortBy(initialSortBy)
    }

    val onPropChange = merge(
        order,
        customSizes,
        visibleCells,
        sortBy,
    )

    fun save(): SerializableTableState {
        val sizes = customSizes.value.mapKeys {
            it.key.id
        }.mapValues {
            it.value.value
        }
        val sortBy = sortBy.value
        return SerializableTableState(
            sizes = sizes,
            sortBy = sortBy?.let {
                SortBy(sortBy.cell.id, sortBy.isUp())
            },
            order = order.value.map { it.id },
            visibleCells = visibleCells.value.map { it.id }
        )
    }

    fun load(s: SerializableTableState) {
        setCustomSizes {
            val cellsThatHaveCustomWidth = findCellById(s.sizes.keys)
            cellsThatHaveCustomWidth.associateWith { s.sizes[it.id]!!.dp }
        }
        setOrder(findCellById(s.order))
        setSortBy(
            s.sortBy?.let { sortBy ->
                findCellById(sortBy.name)?.let {
                    Sort(it as SortableCell<Item>, sortBy.descending)
                }
            }
        )
        setVisibleCells(findCellById(s.visibleCells))
    }


    private fun findCellById(name: String): Cell? {
        return cells.find { it.id == name }
    }

    private fun findCellById(list: Iterable<String>): List<Cell> {
        return list.mapNotNull { name ->
            findCellById(name)
        }
    }

    fun sortedList(list: List<Item>, sortBy: Sort<SortableCell<Item>>? = this.sortBy.value):List<Item> {
        return sortBy.let { sortedBy ->
            if (sortedBy == null) {
                list
            } else {
                list.sortedWith(
                    sortedBy.cell.comparator()
                        .let {
                            if (sortedBy.isUp()) {
                                it.reversed()
                            } else it
                        }
                )
            }
        }
    }

    /**
     * get range of items based on the current sort of table
     */
    fun <ID>getARangeOfItems(
        list: List<Item>,
        id:(Item)->ID,
        fromItem: ID,
        toItem: ID,
    ): List<ID> {
        return sortedList(list).map(id).dropWhile {
            it != fromItem && it != toItem
        }.dropLastWhile {
            it != fromItem && it != toItem
        }
    }

    @Serializable
    data class SerializableTableState(
        val sizes: Map<String, Float> = emptyMap(),
        val sortBy: SortBy? = null,
        val visibleCells: List<String> = emptyList(),
        val order: List<String> = emptyList(),
    )
    @Serializable
    data class SortBy(
        val name: String,
        val descending: Boolean,
    )
}
