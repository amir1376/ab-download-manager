package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.customtable.styled.MyStyledTableHeader
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
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.customtable.CellSize
import com.abdownloadmanager.shared.ui.widget.customtable.CustomCellRenderer
import com.abdownloadmanager.shared.ui.widget.customtable.Table
import com.abdownloadmanager.shared.ui.widget.customtable.TableCell
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.add.TANewDownloadInputs
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

@Composable
fun AddMultiDownloadTable(
    modifier: Modifier,
    component: AddMultiDownloadComponent,
) {
    var isCtrlPressed by remember { mutableStateOf(false) }

    val lastSelectedId = component.lastSelectedId
    val context = AddMultiItemListContext(component, component.isAllSelected)
    val iconProvider = component.fileIconProvider
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
            wrapItem = { _, item, content ->
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
                        downloadChecker = item,
                        onCheckedChange = { dc, b ->
                            component.setSelect(item.credentials.value.link, b)
                        }
                    )
                }

                AddMultiItemTableCells.Name -> {
                    NameCell(
                        downloadUiChecker = item,
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

sealed class AddMultiItemTableCells : TableCell<TANewDownloadInputs> {
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

    data object Name : AddMultiItemTableCells() {
        override val id: String = "Name"
        override val name: StringSource = Res.string.name.asStringSource()
        override val size: CellSize = CellSize.Resizeable(120.dp..1000.dp, 350.dp)
    }

    data object Link : AddMultiItemTableCells() {
        override val id: String = "Link"
        override val name: StringSource = Res.string.link.asStringSource()
        override val size: CellSize = CellSize.Resizeable(120.dp..2000.dp, 240.dp)
    }

    data object SizeCell : AddMultiItemTableCells() {
        override val id: String = "Size"
        override val name: StringSource = Res.string.size.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..180.dp, 100.dp)
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
    downloadUiChecker: TANewDownloadInputs,
    iconProvider: FileIconProvider,
) {
    val name by downloadUiChecker.name.collectAsState()
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
    downloadChecker: TANewDownloadInputs,
) {
    val credentials by downloadChecker.credentials.collectAsState()
    CellText(credentials.link)
}

@Composable
private fun SizeCell(
    downloadChecker: TANewDownloadInputs,
) {
    val length by downloadChecker.lengthStringFlow.collectAsState()
    CellText(
        length.rememberString()
    )
}

@Composable
private fun CheckCell(
    onCheckedChange: (TANewDownloadInputs, Boolean) -> Unit,
    downloadChecker: TANewDownloadInputs,
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
