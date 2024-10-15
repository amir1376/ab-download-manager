package com.abdownloadmanager.desktop.pages.addDownload.multiple

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.desktop.pages.addDownload.shared.*
import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.ui.widget.CheckBox
import com.abdownloadmanager.desktop.ui.widget.Help
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.desktop.utils.windowUtil.moveSafe
import com.abdownloadmanager.utils.category.CategorySelectionMode
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.utils.compose.widget.MyIcon
import java.awt.MouseInfo

@Composable
fun AddMultiItemPage(
    addMultiDownloadComponent: AddMultiDownloadComponent,
) {
    Column(Modifier) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .weight(1f)
        ) {
            WithContentAlpha(1f) {
                Text(
                    "Select Items you want to pick up for download",
                    fontSize = myTextSizes.base
                )
            }
            Spacer(Modifier.height(8.dp))
            AddMultiDownloadTable(
                Modifier.weight(1f),
                addMultiDownloadComponent,
            )
        }
        Footer(
            Modifier,
            addMultiDownloadComponent,
        )
    }
    if (addMultiDownloadComponent.showAddToQueue) {
        ShowAddToQueueDialog(
            queueList = addMultiDownloadComponent.queueList.collectAsState().value,
            onQueueSelected = {
                addMultiDownloadComponent.requestAddDownloads(
                    it
                )
            },
            onClose = {
                addMultiDownloadComponent.closeAddToQueue()
            }
        )
    }
}

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    component: AddMultiDownloadComponent,
) {
    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaveSettings(
                modifier = Modifier.width(300.dp),
                component = component,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Row(Modifier.align(Alignment.Bottom)) {
                ActionButton(
                    text = "Add",
                    onClick = {
                        component.openAddToQueueDialog()
                    },
                    enabled = component.canClickAdd,
                    modifier = Modifier,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = "Cancel",
                    onClick = {
                        component.requestClose()
                    },
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
private fun SaveSettings(
    modifier: Modifier,
    component: AddMultiDownloadComponent,
) {
    Column(
        modifier.animateContentSize()
    ) {
        var dropdownOpen by remember { mutableStateOf(false) }
        val saveMode by component.saveMode.collectAsState()
        Text("Save to:")
        Spacer(Modifier.height(8.dp))
        SaveSolution(
            saveMode = saveMode,
            setSaveMode = {
                component.setSaveMode(it)
            },
            isSelectionOpen = dropdownOpen,
            setSelectionOpen = {
                dropdownOpen = it
            }
        )
        when (saveMode) {
            AddMultiItemSaveMode.EachFileInTheirOwnCategory -> {
                //
            }

            AddMultiItemSaveMode.AllInOneCategory -> {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.height(IntrinsicSize.Max),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategorySelect(
                        categories = component.categories.collectAsState().value,
                        modifier = Modifier.weight(1f),
                        selectedCategory = component.selectedCategory.collectAsState().value,
                        onCategorySelected = {
                            component.setSelectedCategory(it)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    CategoryAddButton(
                        Modifier.fillMaxHeight(),
                        enabled = true,
                        onClick = {
                            component.requestAddCategory()
                        },
                    )
                }
            }

            AddMultiItemSaveMode.InSameLocation -> {
                Spacer(Modifier.height(8.dp))
                AllFilesInSameDirectory(
                    Modifier,
                    folder = component.folder.collectAsState().value,
                    setFolder = {
                        component.setFolder(it)
                    },
                    alsoCategorize = component.alsoAutoCategorize.collectAsState().value,
                    setAlsoCategorize = component::setAlsoAutoCategorize,
                    knownLocations = component.lastUsedLocations.collectAsState().value,
                )
            }
        }
    }
}

@Composable
private fun SaveSolution(
    saveMode: AddMultiItemSaveMode,
    setSaveMode: (AddMultiItemSaveMode) -> Unit,
    isSelectionOpen: Boolean,
    setSelectionOpen: (Boolean) -> Unit,
) {
    SaveSolutionHeader(
        saveMode = saveMode,
        onClick = {
            setSelectionOpen(!isSelectionOpen)
        },
    )
    if (isSelectionOpen) {
        SaveSolutionPopup(
            selectedItem = saveMode,
            onIteSelected = setSaveMode,
            onDismiss = {
                setSelectionOpen(false)
            }
        )
    }
}

@Composable
private fun SaveSolutionPopup(
    selectedItem: AddMultiItemSaveMode,
    onIteSelected: (AddMultiItemSaveMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDialogState(
        size = DpSize(
            height = Dp.Unspecified,
            width = Dp.Unspecified,
        ),
    )
    val close = {
        onDismiss()
    }
    BaseOptionDialog(
        onCloseRequest = close,
        state = state,
        resizeable = false,
    ) {
        LaunchedEffect(window) {
            window.moveSafe(
                MouseInfo.getPointerInfo().location.run {
                    DpOffset(
                        x = x.dp,
                        y = y.dp
                    )
                }
            )
        }
        val shape = RoundedCornerShape(6.dp)
        Column(
            Modifier
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            myColors.surface,
                            myColors.background,
                        )
                    )
                )
        ) {
            WithContentColor(myColors.onBackground) {
                Column(
                    Modifier.widthIn(max = 300.dp)
                ) {
                    WindowDraggableArea(Modifier) {
                        Column(
                            Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Where should each item saved?",
                                Modifier,
                                fontSize = myTextSizes.base
                            )
                            Spacer(Modifier.height(8.dp))
                            WithContentAlpha(0.75f) {
                                Text(
                                    "There are multiple items! please select a way you want to save them",
                                    Modifier,
                                    fontSize = myTextSizes.sm,
                                )
                            }
                        }
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Spacer(
                            Modifier.fillMaxWidth()
                                .height(1.dp)
                                .background(myColors.onBackground / 10),
                        )
                        Spacer(Modifier.height(4.dp))
                        Column {
                            for (item in AddMultiItemSaveMode.entries) {
                                SaveSolutionItem(
                                    title = item.title,
                                    description = item.description,
                                    isSelected = selectedItem == item,
                                    onClick = {
                                        onIteSelected(item)
                                        close()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveSolutionHeader(
    saveMode: AddMultiItemSaveMode,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val borderColor = myColors.onBackground / 0.1f
    val background = myColors.surface / 50
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .height(IntrinsicSize.Max)
            .clip(shape)
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .border(1.dp, borderColor, shape)
            .background(background)
            .clickable(
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        val contentModifier = Modifier
            .padding(vertical = 8.dp)
            .weight(1f)
        Text(
            saveMode.title,
            contentModifier,
        )
        Spacer(
            Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight().padding(vertical = 1.dp)
                .width(1.dp)
                .background(borderColor)
        )
        MyIcon(
            MyIcons.down,
            null,
            Modifier
                .align(Alignment.CenterVertically)
                .size(16.dp),
        )
    }
}

@Composable
private fun SaveSolutionItem(
    title: String,
    description: String,
    isSelected: Boolean?,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        isSelected?.let {
            CheckBox(isSelected, { onClick() }, size = 12.dp)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                title,
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            WithContentAlpha(0.7f) {
                Text(
                    text = description,
                    fontSize = myTextSizes.sm,
                    modifier = Modifier
                )
            }
        }
    }
}


@Composable
private fun AllFilesInSameDirectory(
    modifier: Modifier,
    folder: String,
    setFolder: (String) -> Unit,
    alsoCategorize: Boolean,
    setAlsoCategorize: (Boolean) -> Unit,
    knownLocations: List<String>,
) {
    LocationTextField(
        text = folder,
        setText = {
            setFolder(it)
        },
        modifier = modifier,
        lastUsedLocations = knownLocations
    )
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.onClick {
            setAlsoCategorize(!alsoCategorize)
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBox(
            value = alsoCategorize,
            onValueChange = setAlsoCategorize
        )
        Spacer(Modifier.width(4.dp))
        Text("Auto categorize")
    }
}

enum class AddMultiItemSaveMode(
    val title: String,
    val description: String,
) {
    EachFileInTheirOwnCategory(
        title = "Each item on its own category",
        description = "Each item will be placed in a category that have that file type",
    ),
    AllInOneCategory(
        title = "All items in one Category",
        description = "All files will be saved in the selected category location",
    ),
    InSameLocation(
        title = "All items in one Location",
        description = "All items will be saved in the selected directory",
    );
}