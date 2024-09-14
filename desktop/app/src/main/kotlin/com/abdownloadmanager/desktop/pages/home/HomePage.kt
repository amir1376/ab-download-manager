package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.desktop.pages.home.sections.DownloadList
import com.abdownloadmanager.desktop.pages.home.sections.SearchBox
import com.abdownloadmanager.desktop.pages.home.sections.category.*
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.ui.widget.menu.MenuBar
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.*
import com.abdownloadmanager.desktop.utils.externaldraggable.onExternalDrag
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.amirab.downloader.utils.ByteConverter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.desktop.utils.externaldraggable.DragData


@Composable
fun HomePage(component: HomeComponent) {
    val listState by component.downloadList.collectAsState()
    WindowTitle(AppInfo.name)
    var isDragging by remember { mutableStateOf(false) }

    var showDeletePromptState by remember {
        mutableStateOf(null as DeletePromptState?)
    }

    LaunchedEffect(Unit) {
        component.effects.onEach {
            when (it) {
                is HomeEffects.DeleteItems -> {
                    if (it.list.isNotEmpty()) {
                        showDeletePromptState = DeletePromptState(it.list)
                    }
                }

                else -> {}
            }
        }
            .launchIn(this)
    }
    showDeletePromptState?.let {
        ShowDeletePrompts(
            deletePromptState = it,
            onCancel = {
                showDeletePromptState = null
            },
            onConfirm = {
                showDeletePromptState = null
                component.confirmDelete(it)
            })
    }

    Box(
        Modifier
            .fillMaxSize()
            .onExternalDrag(
                onDragStart = {
                    isDragging = true
                    it.availableDragData.get<DragData.Text>()?.also {
                        component.onExternalTextDraggedIn { it.readText() }
                        return@onExternalDrag
                    }
                    it.availableDragData.get<DragData.FilesList>()?.also {
                        //Caution FileList::readFiles sometimes throws exception
                        component.onExternalFilesDraggedIn { it.readFiles() }
                        return@onExternalDrag
                    }
                },
                onDragExit = {
                    isDragging = false
                    component.onDragExit()
                }
            ) {
                isDragging = false
                component.onDropped()
            }
    ) {
        Column(
            Modifier.alpha(
                animateFloatAsState(if (isDragging) 0.2f else 1f).value
            )
        ) {
            Spacer(Modifier.height(4.dp))
            TopBar(component)
            Spacer(Modifier.height(6.dp))
            Spacer(
                Modifier.fillMaxWidth()
                    .height(1.dp)
                    .background(myColors.surface)
            )
            Row() {
                val categoriesWidth by component.categoriesWidth.collectAsState()
                Categories(
                    Modifier.padding(top = 8.dp)
                        .width(categoriesWidth), component
                )
                Spacer(Modifier.size(8.dp))
                //split pane
                Handle(
                    Modifier.width(5.dp)
                        .fillMaxHeight()
                ) { delta ->
                    component.setCategoriesWidth { it + delta }
                }
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(Modifier.size(8.dp))
                        AddUrlButton {
                            component.requestAddNewDownload()
                        }
                        Actions(component.headerActions)
                    }
                    var lastSelected by remember { mutableStateOf(null as Long?) }
                    DownloadList(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        downloadList = listState,
                        downloadOptions = component.downloadOptions.collectAsState().value,
                        onRequestCloseOption = {
                            component.onRequestCloseDownloadItemOption()
                        },
                        onRequestOpenOption = { itemState ->
                            component.onRequestOpenDownloadItemOption(itemState)
                        },
                        selectionList = component.selectionList.collectAsState().value,
                        onItemSelectionChange = { id, checked ->
                            lastSelected = id
                            component.onItemSelectionChange(id, checked)
                        },
                        onRequestOpenDownload = {
                            component.openFileOrShowProperties(it)
                        },
                        onNewSelection = {
                            component.newSelection(ids = it)
                        },
                        lastSelectedId = lastSelected,
                        tableState = component.tableState,
                    )
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                myColors.surface
                            )
                    )
                    Footer(component)
                }
            }
        }
        NotificationArea(
            Modifier
                .width(300.dp)
                .padding(24.dp)
                .align(Alignment.BottomEnd)
        )
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            DragWidget(
                Modifier.fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                component.currentActiveDrops.value?.size,
            )
        }
    }
}

@Composable
private fun ShowDeletePrompts(
    deletePromptState: DeletePromptState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Dialog(onDismissRequest = onCancel) {
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
                .padding(16.dp)
                .width(IntrinsicSize.Max)
                .widthIn(max = 260.dp)
        ) {
            Text(
                "Confirm Delete",
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Are you sure you want to delete ${deletePromptState.downloadList.size} item ?",
                fontSize = myTextSizes.base,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    deletePromptState.alsoDeleteFile = !deletePromptState.alsoDeleteFile
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckBox(deletePromptState.alsoDeleteFile, {
                    deletePromptState.alsoDeleteFile = it
                })
                Spacer(Modifier.width(8.dp))
                Text(
                    "Also delete file from disk",
                    fontSize = myTextSizes.base,
                    color = myColors.onBackground,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                ActionButton(
                    text = "Delete",
                    onClick = onConfirm,
                    borderColor = SolidColor(myColors.error),
                    contentColor = myColors.error,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(text = "Cancel", onClick = onCancel)
            }
        }
    }
}

@Stable
class DeletePromptState(
    val downloadList: List<Long>,
) {
    var alsoDeleteFile by mutableStateOf(false)
}

@Composable
fun DragWidget(
    modifier: Modifier,
    linkCount: Int?,
) {
    val shape = RoundedCornerShape(12.dp)
    val background = myColors.onBackground / 10
    Column(
        modifier
            .clip(shape)
            .background(background)
            .padding(8.dp)
            .dashedBorder(
                shape = shape,
                width = 2.dp,
                color = myColors.onBackground,
                on = 1.dp,
                off = 4.dp
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MyIcon(
            MyIcons.download,
            null,
            Modifier.size(36.dp),
        )
        Text(
            text = "Drop link or file here.",
            fontSize = myTextSizes.xl
        )
        if (linkCount != null) {
            when {
                linkCount > 0 -> {
                    Text(
                        "$linkCount links will be imported",
                        fontSize = myTextSizes.base,
                        color = myColors.success,
                    )
                }

                linkCount == 0 -> {
                    Text("Nothing will be imported")
                }
            }

        }

    }


}

@Composable
private fun Categories(
    modifier: Modifier,
    component: HomeComponent,
) {
    val currentTypeFilter = component.filterState.typeCategoryFilter
    val currentStatusFilter = component.filterState.statusFilter

    val clipShape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .padding(start = 16.dp)
            .clip(clipShape)
            .border(1.dp, myColors.surface, clipShape)
            .verticalScroll(rememberScrollState())
    ) {
        var expendedItem: DownloadStatusCategoryFilter? by remember { mutableStateOf(currentStatusFilter) }
        for (statusCategoryFilter in DefinedStatusCategories.values()) {
            StatusFilterItem(
                isExpanded = expendedItem == statusCategoryFilter,
                currentTypeCategoryFilter = currentTypeFilter,
                currentStatusCategoryFilter = currentStatusFilter,
                statusFilter = statusCategoryFilter,
                typeFilter = DefinedTypeCategories.values(),
                onFilterChange = {
                    component.onFilterChange(statusCategoryFilter, it)
                },
                onRequestExpand = { expand ->
                    expendedItem = statusCategoryFilter.takeIf { expand }
                }
            )
        }
    }
}

@Composable
private fun HomeMenuBar(component: HomeComponent) {
    val menu = component.menu
    MenuBar(menu)
}

@Composable
private fun Footer(component: HomeComponent) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.weight(1f))
        val activeCount by component.activeDownloadCountFlow.collectAsState()
        FooterItem(MyIcons.activeCount, activeCount.toString(), "")
        val size by component.globalSpeedFlow.collectAsState(0)
        val speed = baseConvertBytesToHumanReadable(size)
        if (speed != null) {
            val speedText = ByteConverter.prettify(speed.value)
            val unitText = ByteConverter.unitPrettify(speed.unit)
                ?.let {
                    "$it/s"
                }
                .orEmpty()
            FooterItem(MyIcons.speed, speedText, unitText)
        }
    }
}

@Composable
private fun FooterItem(icon: IconSource, value: String, unit: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WithContentAlpha(0.25f) {
            MyIcon(icon, null, Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        WithContentAlpha(0.75f) {
            Text(value, maxLines = 1, fontSize = myTextSizes.base)
        }
        Spacer(Modifier.width(8.dp))
        WithContentAlpha(0.25f) {
            Text(unit, maxLines = 1, fontSize = myTextSizes.base)
        }
    }
}

@Composable
private fun TopBar(component: HomeComponent) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeMenuBar(component)
        Box(Modifier.weight(1f))
        val searchBoxInteractionSource = remember { MutableInteractionSource() }

        val isFocused by searchBoxInteractionSource.collectIsFocusedAsState()
        SearchBox(
            text = component.filterState.textToSearch,
            onTextChange = {
                component.filterState.textToSearch = it
            },
            interactionSource = searchBoxInteractionSource,
            modifier = Modifier
                .width(
                    animateDpAsState(
                        if (isFocused) 220.dp else 180.dp
                    ).value
                )

        )
    }
}


