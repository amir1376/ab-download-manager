package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.desktop.pages.addDownload.DownloadUiChecker
import com.abdownloadmanager.desktop.ui.WithContentAlpha
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.ui.widget.customtable.*
import com.abdownloadmanager.desktop.ui.widget.customtable.styled.MyStyledTableHeader
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AddMultiDownloadTable(
    modifier: Modifier,
    component: AddMultiDownloadComponent,
) {
    var isCtrlPressed by remember { mutableStateOf(false) }

    val lastSelectedId = component.lastSelectedId
    val context = AddMultiItemListContext(component, component.isAllSelected)
    CompositionLocalProvider(
        LocalAddMultiItemListContext provides context,
    ) {
        val itemHorizontalPadding = 16.dp
        Table(
            tableState = component.tableState,
            list = component.list,
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
            wrapItem = { item, content ->
                val shape = RoundedCornerShape(12.dp)
                WithContentAlpha(1f) {
                    val isSelected = remember(item, component.selectionList) {
                        component.isSelected(item)
                    }
                    CompositionLocalProvider(
                        LocalIsChecked provides isSelected,
                    ) {
                        val credentials by item.credentials.collectAsState()
                        val itemInteractionSource = remember { MutableInteractionSource() }
                        Box(
                            Modifier
                                .widthIn(min = getTableSize().visibleWidth)
                                .onClick(
                                    interactionSource = itemInteractionSource
                                ) {
                                    if (isCtrlPressed) {
                                        context.select(credentials.link, !isSelected)
                                    } else {
                                        context.changeAllSelection(false)
                                        context.select(credentials.link, true)
                                    }
                                }
                                .onClick(
                                    enabled = lastSelectedId != null,
                                    keyboardModifiers = {
                                        this.isShiftPressed
                                    }
                                ) {
                                    val lastSelected = lastSelectedId ?: return@onClick
                                    val currentId = credentials.link
                                    val ids = component.tableState.getARangeOfItems(
                                        list = component.list,
                                        id = { it.credentials.value.link },
                                        fromItem = lastSelected,
                                        toItem = currentId,
                                    )
                                    context.newSelection(ids, true)
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
                                .padding(vertical = 2.dp, horizontal = itemHorizontalPadding)
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
                        downloadChecker = item,
                        onCheckedChange = { dc, b ->
                            component.setSelect(item.credentials.value.link, b)
                        }
                    )
                }

                AddMultiItemTableCells.Name -> {
                    NameCell(item)
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
    val component: AddMultiDownloadComponent,
    val isAllSelected: Boolean,
) {
    fun changeAllSelection(boolean: Boolean) {
        component.selectAll(boolean)
    }

    fun select(id: String, boolean: Boolean) {
        component.setSelect(id, boolean)
    }

    fun newSelection(ids: List<String>, boolean: Boolean) {
        component.resetSelectionTo(ids, boolean)
    }
}

sealed class AddMultiItemTableCells : TableCell<DownloadUiChecker> {
    companion object {
        fun all(): List<AddMultiItemTableCells> {
            return listOf(
                Check,
                Name,
                Link,
                SizeCell,
            )
        }
    }

    data object Check : AddMultiItemTableCells(),
        CustomCellRenderer {
        override val name: String = "#"
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

    data object Name : AddMultiItemTableCells() {
        override val name: String = "Name"
        override val size: CellSize = CellSize.Resizeable(120.dp..300.dp, 160.dp)
    }

    data object Link : AddMultiItemTableCells() {
        override val name: String = "Link"
        override val size: CellSize = CellSize.Resizeable(120.dp..300.dp, 120.dp)
    }

    data object SizeCell : AddMultiItemTableCells() {
        override val name: String = "Size"
        override val size: CellSize = CellSize.Resizeable(120.dp..180.dp)
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
    it: DownloadUiChecker
) {
    val name by it.name.collectAsState()
    CellText(name)
}

@Composable
private fun LinkCell(
    downloadChecker: DownloadUiChecker
) {
    val credentials by downloadChecker.credentials.collectAsState()
    CellText(credentials.link)
}

@Composable
private fun SizeCell(
    downloadChecker: DownloadUiChecker
) {
    val length by downloadChecker.length.collectAsState()
    CellText(
        length?.let {
            convertSizeToHumanReadable(it)
        } ?: ""
    )
}

@Composable
private fun CheckCell(
    onCheckedChange: (DownloadUiChecker, Boolean) -> Unit,
    downloadChecker: DownloadUiChecker,
) {
    val isChecked = LocalIsChecked.current
    CheckBox(
        value = isChecked,
        onValueChange = {
            onCheckedChange(downloadChecker, it)
        },
        modifier = Modifier,
        size = 12.dp,
    )
}
