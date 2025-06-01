package com.abdownloadmanager.desktop.pages.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuBarScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.WindowScope
import com.abdownloadmanager.desktop.pages.home.sections.DownloadList
import com.abdownloadmanager.desktop.pages.home.sections.SearchBox
import com.abdownloadmanager.desktop.pages.home.sections.category.DefinedStatusCategories
import com.abdownloadmanager.desktop.pages.home.sections.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.desktop.pages.home.sections.category.StatusFilterItem
import com.abdownloadmanager.desktop.window.custom.TitlePosition
import com.abdownloadmanager.desktop.window.custom.WindowEnd
import com.abdownloadmanager.desktop.window.custom.WindowStart
import com.abdownloadmanager.desktop.window.custom.WindowTitlePosition
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.ui.widget.menu.MenuBar
import com.abdownloadmanager.shared.ui.widget.menu.ShowOptionsInDropDown
import com.abdownloadmanager.shared.utils.LocalSpeedUnit
import com.abdownloadmanager.shared.utils.category.Category
import com.abdownloadmanager.shared.utils.category.rememberIconPainter
import com.abdownloadmanager.shared.utils.convertPositiveBytesToSizeUnit
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.WithTitleBarDirection
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.compose.localizationmanager.WithLanguageDirection
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.datatransfer.DataFlavor
import java.io.File


@Composable
fun FrameWindowScope.HomePage(component: HomeComponent) {
    val listState by component.downloadList.collectAsState()
    var isDragging by remember { mutableStateOf(false) }

    var showDeletePromptState by remember {
        mutableStateOf(null as DeletePromptState?)
    }

    var showDeleteCategoryPromptState by remember {
        mutableStateOf(null as CategoryDeletePromptState?)
    }

    var showConfirmPrompt by remember {
        mutableStateOf(null as ConfirmPromptState?)
    }

    LaunchedEffect(Unit) {
        component.effects.onEach {
            when (it) {
                is HomeEffects.DeleteItems -> {
                    if (it.list.isNotEmpty()) {
                        showDeletePromptState = DeletePromptState(
                            downloadList = it.list,
                            finishedCount = it.finishedCount,
                            unfinishedCount = it.unfinishedCount,
                        )
                    }
                }

                is HomeEffects.DeleteCategory -> {
                    showDeleteCategoryPromptState = CategoryDeletePromptState(it.category)
                }

                is HomeEffects.AutoCategorize -> {
                    showConfirmPrompt = ConfirmPromptState(
                        title = Res.string.confirm_auto_categorize_downloads_title.asStringSource(),
                        description = Res.string.confirm_auto_categorize_downloads_description.asStringSource(),
                        onConfirm = component::onConfirmAutoCategorize
                    )
                }

                is HomeEffects.ResetCategoriesToDefault -> {
                    showConfirmPrompt = ConfirmPromptState(
                        title = Res.string.confirm_reset_to_default_categories_title.asStringSource(),
                        description = Res.string.confirm_reset_to_default_categories_description.asStringSource(),
                        onConfirm = component::onConfirmResetCategories
                    )
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
    showDeleteCategoryPromptState?.let {
        ShowDeleteCategoryPrompt(
            deletePromptState = it,
            onCancel = {
                showDeleteCategoryPromptState = null
            },
            onConfirm = {
                showDeleteCategoryPromptState = null
                component.onConfirmDeleteCategory(it)
            })
    }
    showConfirmPrompt?.let {
        ShowConfirmPrompt(
            promptState = it,
            onCancel = {
                showConfirmPrompt = null
            },
            onConfirm = {
                showConfirmPrompt?.onConfirm?.invoke()
                showConfirmPrompt = null
            }
        )
    }
    val mergeTopBar = shouldMergeTopBarWithTitleBar(component)
    if (mergeTopBar) {
        WindowTitlePosition(
            TitlePosition(
                centered = true,
                afterStart = true,
                padding = PaddingValues(end = 32.dp)
            )
        )
        WindowStart {
            HomeMenuBar(component, Modifier.fillMaxHeight())
        }
        WindowEnd {
            HomeSearch(
                component = component,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
            )
        }
    } else {
        WindowTitlePosition(
            TitlePosition(centered = false, afterStart = false)
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = {
                    if (it.awtTransferable.isDataFlavorSupported(DownloadItemListDataFlavor)) {
                        // this item is ours we don't want to use our download item for import list usage
                        return@dragAndDropTarget false
                    } else it.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                            it.awtTransferable.isDataFlavorSupported(DataFlavor.stringFlavor)
                },
                target = remember {
                    object : DragAndDropTarget {
                        private fun onDraggedIn(event: DragAndDropEvent) {
                            if (event.awtTransferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                component.onExternalTextDraggedIn {
                                    (event.awtTransferable.getTransferData(
                                        DataFlavor.stringFlavor
                                    ) as String)
                                }
                                return
                            }

                            if (event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                component.onExternalFilesDraggedIn {
                                    (event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>).filterIsInstance<File>()
                                }
                                return
                            }
                        }

                        override fun onStarted(event: DragAndDropEvent) {
                            isDragging = true
                            onDraggedIn(event)
                        }

                        override fun onEnded(event: DragAndDropEvent) {
                            isDragging = false
                            component.onDragExit()
                        }

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            isDragging = false
                            if (Platform.isMac()) {
                                onDraggedIn(event)
                            }
                            component.onDropped()
                            return true
                        }
                    }
                }
            )
    ) {
        Column(
            Modifier.alpha(
                animateFloatAsState(if (isDragging) 0.2f else 1f).value
            )
        ) {
            if (!mergeTopBar) {
                WithTitleBarDirection {
                    Spacer(Modifier.height(4.dp))
                    TopBar(component)
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(
                Modifier.fillMaxWidth()
                    .height(1.dp)
                    .background(myColors.surface)
            )
            Row {
                val categoriesWidth by component.categoriesWidth.collectAsState()
                Categories(
                    modifier = Modifier.padding(top = 8.dp)
                        .width(categoriesWidth),
                    component = component
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
                        Spacer(Modifier.size(4.dp))
                        AddUrlButton {
                            component.requestAddNewDownload()
                        }
                        Actions(
                            component.headerActions,
                            component.showLabels.collectAsState().value
                        )
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
                        fileIconProvider = component.fileIconProvider,
                        categoryManager = component.categoryManager,
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
private fun shouldMergeTopBarWithTitleBar(component: HomeComponent): Boolean {
    val mergeTopBarWithTitleBarInSettings = component.mergeTopBarWithTitleBar.collectAsState().value
    if (!mergeTopBarWithTitleBarInSettings) return false
    val density = LocalDensity.current
    val widthDp = density.run {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    return widthDp > 700.dp
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
                myStringResource(Res.string.confirm_delete_download_items_title),
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            val finishedCount = deletePromptState.finishedCount
            val unfinishedCount = deletePromptState.unfinishedCount
            Text(
                when {
                    deletePromptState.hasBothFinishedAndUnfinished() -> {
                        Res.string.confirm_delete_download_finished_and_unfinished_items_description.asStringSourceWithARgs(
                            Res.string.confirm_delete_download_finished_and_unfinished_items_description_createArgs(
                                finishedCount = finishedCount.toString(),
                                unfinishedCount = unfinishedCount.toString(),
                            )
                        )
                    }

                    deletePromptState.hasUnfinishedDownloads -> {
                        Res.string.confirm_delete_download_unfinished_items_description.asStringSourceWithARgs(
                            Res.string.confirm_delete_download_unfinished_items_description_createArgs(
                                count = unfinishedCount.toString(),
                            )
                        )
                    }

                    else -> {
                        Res.string.confirm_delete_download_items_description.asStringSourceWithARgs(
                            Res.string.confirm_delete_download_items_description_createArgs(
                                count = finishedCount.toString()
                            ),
                        )
                    }
                }.rememberString(),
                fontSize = myTextSizes.base,
                color = myColors.onBackground,
            )
            if (deletePromptState.hasFinishedDownloads) {
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
                        myStringResource(Res.string.also_delete_file_from_disk),
                        fontSize = myTextSizes.base,
                        color = myColors.onBackground,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                ActionButton(
                    text = myStringResource(Res.string.delete),
                    onClick = onConfirm,
                    borderColor = SolidColor(myColors.error),
                    contentColor = myColors.error,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(text = myStringResource(Res.string.cancel), onClick = onCancel)
            }
        }
    }
}

@Composable
private fun ShowConfirmPrompt(
    promptState: ConfirmPromptState,
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
                text = promptState.title.rememberString(),
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = promptState.description.rememberString(),
                fontSize = myTextSizes.base,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                ActionButton(
                    text = myStringResource(Res.string.ok),
                    onClick = onConfirm,
                    contentColor = myColors.error,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(text = myStringResource(Res.string.cancel), onClick = onCancel)
            }
        }
    }
}

@Composable
private fun ShowDeleteCategoryPrompt(
    deletePromptState: CategoryDeletePromptState,
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
                myStringResource(
                    Res.string.confirm_delete_category_item_title,
                    Res.string.confirm_delete_category_item_title_createArgs(
                        name = deletePromptState.category.name
                    ),
                ),
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                myStringResource(
                    Res.string.confirm_delete_category_item_description,
                    Res.string.confirm_delete_category_item_description_createArgs(
                        value = deletePromptState.category.name
                    )
                ),
                fontSize = myTextSizes.base,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                myStringResource(Res.string.your_download_will_not_be_deleted),
                fontSize = myTextSizes.base,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                ActionButton(
                    text = myStringResource(Res.string.delete),
                    onClick = onConfirm,
                    borderColor = SolidColor(myColors.error),
                    contentColor = myColors.error,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(text = myStringResource(Res.string.cancel), onClick = onCancel)
            }
        }
    }
}

@Stable
class DeletePromptState(
    val downloadList: List<Long>,
    val finishedCount: Int,
    val unfinishedCount: Int,
) {
    val hasFinishedDownloads = finishedCount > 0
    var hasUnfinishedDownloads = unfinishedCount > 0
    var alsoDeleteFile by mutableStateOf(false)

    fun hasBothFinishedAndUnfinished(): Boolean {
        return hasFinishedDownloads && hasUnfinishedDownloads
    }
}

@Immutable
data class CategoryDeletePromptState(
    val category: Category,
)

@Immutable
data class ConfirmPromptState(
    val title: StringSource,
    val description: StringSource,
    val onConfirm: () -> Unit,
)

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
            text = myStringResource(Res.string.drop_link_or_file_here),
            fontSize = myTextSizes.xl
        )
        if (linkCount != null && Platform.isMac().not()) {
            when {
                linkCount > 0 -> {
                    Text(
                        myStringResource(
                            Res.string.n_links_will_be_imported,
                            Res.string.n_links_will_be_imported_createArgs(
                                count = linkCount.toString()
                            )
                        ),
                        fontSize = myTextSizes.base,
                        color = myColors.success,
                    )
                }

                linkCount == 0 -> {
                    Text(myStringResource(Res.string.nothing_will_be_imported))
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
    val categories by component.categoryManager.categoriesFlow.collectAsState()
    val clipShape = RoundedCornerShape(6.dp)
    val showCategoryOption by component.categoryActions.collectAsState()

    fun showCategoryOption(item: Category?) {
        component.showCategoryOptions(item)
    }

    fun closeCategoryOptions() {
        component.closeCategoryOptions()
    }
    Column(
        modifier
            .padding(start = 16.dp)
            .clip(clipShape)
            .border(1.dp, myColors.surface, clipShape)
            .padding(1.dp)
            .verticalScroll(rememberScrollState())
    ) {
        var expendedItem: DownloadStatusCategoryFilter? by remember {
            mutableStateOf(
                currentStatusFilter
            )
        }
        for (statusCategoryFilter in DefinedStatusCategories.values()) {
            StatusFilterItem(
                isExpanded = expendedItem == statusCategoryFilter,
                currentTypeCategoryFilter = currentTypeFilter,
                currentStatusCategoryFilter = currentStatusFilter,
                statusFilter = statusCategoryFilter,
                categories = categories,
                onFilterChange = {
                    component.onFilterChange(statusCategoryFilter, it)
                },
                onRequestExpand = { expand ->
                    expendedItem = statusCategoryFilter.takeIf { expand }
                },
                onItemsDroppedInCategory = { category, ids ->
                    component.moveItemsToCategory(category, ids)
                },
                onRequestOpenOptionMenu = {
                    showCategoryOption(it)
                }
            )
        }
    }
    showCategoryOption?.let {
        CategoryOption(
            categoryOptionMenuState = it,
            onDismiss = {
                closeCategoryOptions()
            }
        )
    }
}

@Composable
fun CategoryOption(
    categoryOptionMenuState: CategoryActions,
    onDismiss: () -> Unit,
) {
    ShowOptionsInDropDown(
        MenuItem.SubMenu(
            icon = categoryOptionMenuState.categoryItem?.rememberIconPainter(),
            title = categoryOptionMenuState.categoryItem?.name.orEmpty().asStringSource(),
            categoryOptionMenuState.menu,
        ),
        onDismiss
    )
}

@Composable
private fun FrameWindowScope.HomeMenuBar(
    component: HomeComponent,
    modifier: Modifier,
) {
    val nativeMenuBarWithTitleBarInSettings by component.showNativeMenuBar.collectAsState()
    val menu = component.menu
    if (nativeMenuBarWithTitleBarInSettings) {
        NativeMenuBar(menu)
    } else {
        MenuBar(
            modifier,
            menu
        )
    }
}

@Composable
private fun FrameWindowScope.NativeMenuBar(menu: List<MenuItem.SubMenu>) {
    MenuBar {
        menu.forEach { item ->
            val items by item.items.collectAsState()
            val title by item.title.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            Menu(title.rememberString(), enabled = enabled) {
                items.forEach { renderMenuItem(it) }
            }
        }
    }
}

@Composable
fun MenuScope.renderMenuItem(item: MenuItem) {
    when (item) {
        is MenuItem.SubMenu -> {
            val items by item.items.collectAsState()
            val title by item.title.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            Menu(title.rememberString(), enabled = enabled) {
                items.forEach { renderMenuItem(it) }
            }
        }

        is MenuItem.Separator -> Separator()
        is MenuItem.SingleItem -> {
            val title by item.title.collectAsState()
            val icon by item.icon.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            Item(
                title.rememberString(),
                onClick = item::onClick,
                icon = icon?.rememberPainter(),
                enabled = enabled
            )
        }
    }
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
        val speed = convertPositiveBytesToSizeUnit(size, LocalSpeedUnit.current)
        if (speed != null) {
            val speedText = speed.formatedValue()
            val unitText = speed.unit.toString() + "/s"
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
private fun FrameWindowScope.TopBar(component: HomeComponent) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeMenuBar(component, Modifier)
        Box(Modifier.weight(1f))
        HomeSearch(
            component = component,
            modifier = Modifier,
            textPadding = PaddingValues(8.dp),
        )
    }
}

@Composable
fun HomeSearch(
    component: HomeComponent,
    modifier: Modifier,
    textPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
) {
    val searchBoxInteractionSource = remember { MutableInteractionSource() }

    val isFocused by searchBoxInteractionSource.collectIsFocusedAsState()
    WithLanguageDirection {
        SearchBox(
            text = component.filterState.textToSearch,
            onTextChange = {
                component.filterState.textToSearch = it
            },
            textPadding = textPadding,
            interactionSource = searchBoxInteractionSource,
            modifier = modifier
                .width(
                    animateDpAsState(
                        if (isFocused) 220.dp else 180.dp
                    ).value
                )
        )
    }
}


