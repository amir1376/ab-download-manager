package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.table.customtable.styled.MyStyledTableHeader
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.table.customtable.CellSize
import com.abdownloadmanager.shared.ui.widget.table.customtable.CustomCellRenderer
import com.abdownloadmanager.shared.ui.widget.table.customtable.Table
import com.abdownloadmanager.shared.ui.widget.table.customtable.TableCell
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputsUniqueIdType
import com.abdownloadmanager.shared.pages.adddownload.multiple.NewMultiDownloadState
import com.abdownloadmanager.shared.ui.widget.table.customtable.SortableCell
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

@Composable
fun AddMultiDownloadTable(
    modifier: Modifier,
    component: DesktopAddMultiDownloadComponent,
) {
    var isCtrlPressed by remember { mutableStateOf(false) }

    val lastSelectedId = component.lastSelectedId
    val context = AddMultiItemListContext(component, component.isAllFilteredSelected.collectAsState().value)
    val iconProvider = component.fileIconProvider
    val list by component.filteredList.collectAsState()

    CompositionLocalProvider(
        LocalAddMultiItemListContext provides context,
    ) {
        val itemHorizontalPadding = 16.dp
        Table(
            key = {
                it.id
            },
            tableState = component.tableState,
            list = list,
            modifier = modifier
                .onKeyEvent {
                    isCtrlPressed = it.isCtrlPressed
                    false
                }
                .onKeyEvent {
                    if (it.key == Key.Escape) {
                        context.changeAllSelection(false)
                        true
                    } else {
                        false
                    }
                }
                .onKeyEvent {
                    if (isCtrlPressed && it.key == Key.A) {
                        context.changeAllSelection(true)
                        true
                    } else {
                        false
                    }
                },
            wrapHeader = {
                MyStyledTableHeader(
                    itemHorizontalPadding = itemHorizontalPadding,
                    content = it
                )
            },
            wrapItem = { _, item, content ->
                val shape = RoundedCornerShape(12.dp)
                WithContentAlpha(1f) {
                    val isSelected = remember(item, component.selectionList) {
                        component.isSelected(item.id)
                    }
                    CompositionLocalProvider(
                        LocalIsChecked provides isSelected,
                    ) {
                        val itemInteractionSource = remember { MutableInteractionSource() }
                        Box(
                            Modifier
                                .widthIn(min = getTableSize().visibleWidth)
                                .onClick(
                                    interactionSource = itemInteractionSource
                                ) {
                                    if (isCtrlPressed) {
                                        context.select(item.id, !isSelected)
                                    } else {
                                        context.changeAllSelection(false)
                                        context.select(item.id, true)
                                    }
                                }
                                .onClick(
                                    enabled = lastSelectedId != null,
                                    keyboardModifiers = {
                                        this.isShiftPressed
                                    }
                                ) {
                                    val lastSelected = lastSelectedId ?: return@onClick
                                    val currentId = item.id
                                    val ids = component.tableState.getARangeOfItems(
                                        list = list,
                                        id = { it.id },
                                        fromItem = lastSelected,
                                        toItem = currentId,
                                    )
                                    context.newSelection(ids, true)
                                }
                                .onClick(
                                    matcher = PointerMatcher.mouse(PointerButton.Secondary)
                                ) {
                                    component.openConfigurableList(item.id)
                                }
                                .fillMaxWidth()
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
                                .padding(vertical = 8.dp, horizontal = itemHorizontalPadding)
                        ) {
                            content()
                        }
                    }
                }
            }
        ) { cell, item ->
            when (cell) {
                AddMultiItemTableCells.Check -> {
                    CheckCell(
                        newMultiDownloadState = item,
                        onCheckedChange = { dc, b ->
                            component.setSelect(item.id, b)
                        }
                    )
                }

                AddMultiItemTableCells.Name -> {
                    NameCell(
                        item = item,
                        iconProvider = iconProvider,
                    )
                }

                AddMultiItemTableCells.Link -> {
                    LinkCell(item)
                }

                AddMultiItemTableCells.SizeCell -> {
                    SizeCell(item)
                }
            }

        }
    }
}

private val LocalIsChecked = compositionLocalOf<Boolean> {
    error("LocalIsChecked not provided")
}
private val LocalAddMultiItemListContext = compositionLocalOf<AddMultiItemListContext> {
    error("LocalAddMultiItemListContext not provided")
}

class AddMultiItemListContext(
    val component: DesktopAddMultiDownloadComponent,
    val isAllSelected: Boolean,
) {
    fun changeAllSelection(boolean: Boolean) {
        component.selectAll(boolean)
    }

    fun select(id: NewDownloadInputsUniqueIdType, boolean: Boolean) {
        component.setSelect(id, boolean)
    }

    fun newSelection(ids: List<NewDownloadInputsUniqueIdType>, boolean: Boolean) {
        component.resetSelectionTo(ids, boolean)
    }
}

sealed class AddMultiItemTableCells : TableCell<NewMultiDownloadState> {
    companion object {
        fun all(): List<AddMultiItemTableCells> {
            return listOf(
                Check,
                Name,
                SizeCell,
                Link,
            )
        }
    }

    data object Check : AddMultiItemTableCells(),
        CustomCellRenderer {
        override val id: String = "#"
        override val name: StringSource = "#".asStringSource()
        override val size: CellSize = CellSize.Fixed(26.dp)

        @Composable
        override fun drawHeader() {
            val context = LocalAddMultiItemListContext.current
            CheckBox(
                context.isAllSelected,
                { context.component.selectAll(!context.isAllSelected) },
                size = 12.dp
            )
        }
    }

    data object Name : AddMultiItemTableCells(), SortableCell<NewMultiDownloadState> {
        override val id: String = "Name"
        override val name: StringSource = Res.string.name.asStringSource()
        override val size: CellSize = CellSize.Resizeable(120.dp..1000.dp, 350.dp)
        override fun comparator(): Comparator<NewMultiDownloadState> {
            return compareBy { it.name }
        }
    }

    data object Link : AddMultiItemTableCells(), SortableCell<NewMultiDownloadState> {
        override val id: String = "Link"
        override val name: StringSource = Res.string.link.asStringSource()
        override val size: CellSize = CellSize.Resizeable(120.dp..2000.dp, 240.dp)
        override fun comparator(): Comparator<NewMultiDownloadState> {
            return compareBy { it.link }
        }
    }

    data object SizeCell : AddMultiItemTableCells(), SortableCell<NewMultiDownloadState> {
        override val id: String = "Size"
        override val name: StringSource = Res.string.size.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..180.dp, 100.dp)
        override fun comparator(): Comparator<NewMultiDownloadState> {
            return compareBy { it.size }
        }
    }
}


@Composable
private fun CellText(
    text: String,
) {
    Text(
        text,
        fontSize = myTextSizes.base,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun NameCell(
    item: NewMultiDownloadState,
    iconProvider: FileIconProvider,
) {
    val name = item.name
    val icon = iconProvider.rememberIcon(name)
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp).alpha(0.75f)
        )
        Spacer(Modifier.width(8.dp))
        CellText(name)
    }

}

@Composable
private fun LinkCell(
    item: NewMultiDownloadState,
) {
    CellText(item.link)
}

@Composable
private fun SizeCell(
    multiDownloadState: NewMultiDownloadState,
) {
    CellText(
        multiDownloadState.sizeString.rememberString()
    )
}

@Composable
private fun CheckCell(
    onCheckedChange: (NewMultiDownloadState, Boolean) -> Unit,
    newMultiDownloadState: NewMultiDownloadState,
) {
    val isChecked = LocalIsChecked.current
    CheckBox(
        value = isChecked,
        onValueChange = {
            onCheckedChange(newMultiDownloadState, it)
        },
        modifier = Modifier,
        size = 12.dp,
    )
}
