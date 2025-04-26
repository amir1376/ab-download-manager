package com.abdownloadmanager.desktop.pages.queue

import com.abdownloadmanager.desktop.utils.configurable.ConfigurableGroup
import com.abdownloadmanager.desktop.utils.configurable.RenderConfigurableGroup
import com.abdownloadmanager.shared.utils.ui.LocalContentAlpha
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.div
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.*
import org.burnoutcrew.reorderable.*


@Composable
fun QueuePage(component: QueuesComponent) {
    val queues = component.queuesState
    val activeItem: DownloadQueue = component.selectedItem
    WindowTitle(myStringResource(Res.string.queues))
    val borderShape = RoundedCornerShape(6.dp)
    val borderColor = myColors.onBackground / 5
    Column {
        Row(
            Modifier.weight(1f)
        ) {
            QueueListSection(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(200.dp)
                    .padding(2.dp)
                    .border(1.dp, borderColor, borderShape)
                    .clip(borderShape)
                    .padding(1.dp)
                    .fillMaxHeight(),
                queues = queues,
                selectedItem = component.selectedItem.id,
                setSelected = { id ->
                    component.onQueueSelected(id)
                },
                component = component
            )
            QueueInfo(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .border(1.dp, borderColor, borderShape),
                item = activeItem,
                component = component.queueInfoComponent.collectAsState().value.child!!.instance,
            )
        }
        Actions(component, activeItem)
    }
}

@Composable
private fun Actions(
    component: QueuesComponent,
    selectedItem: DownloadQueue,
) {
    val isActive by selectedItem.activeFlow.collectAsState()
    val scope = rememberCoroutineScope()
    Row(
        Modifier.fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .padding(horizontal = 16.dp)
            .padding(vertical = 16.dp),
    ) {
        val space = @Composable {
            Spacer(Modifier.width(4.dp))
        }
        ActionButton(
            text = myStringResource(
                if (isActive) {
                    Res.string.stop_queue
                } else {
                    Res.string.start_queue
                }
            ),
            modifier = Modifier,
            onClick = {
                scope.launch {
                    if (isActive) {
                        selectedItem.stop()
                    } else {
                        selectedItem.start()
                    }
                }
            }
        )
        space()
        ActionButton(
            text = myStringResource(Res.string.close),
            modifier = Modifier,
            onClick = {
                component.close()
            }
        )
    }
}

enum class QueueInfoPages(val title: StringSource, val icon: IconSource) {
    Config(Res.string.config.asStringSource(), MyIcons.settings),
    Items(Res.string.items.asStringSource(), MyIcons.queue),
}

@Composable
private fun QueueInfo(
    modifier: Modifier,
    item: DownloadQueue,
    component: QueueInfoComponent,
) {
    val fm = LocalFocusManager.current
    //remove focus to prevent accidentally change config in different queue
    LaunchedEffect(item) {
        fm.clearFocus()
    }
    var currentPage by remember {
        mutableStateOf(QueueInfoPages.Config)
    }
    Column(modifier) {
        val shape = RoundedCornerShape(6.dp)
        Column(
            Modifier
                .clip(shape)
                .background(myColors.surface)
                .padding(horizontal = 4.dp)
                .padding(top = 1.dp)
                .padding(bottom = 1.dp)
        ) {
            MyTabRow {
                QueueInfoPages.entries.forEach {
                    MyTab(
                        selected = it == currentPage,
                        onClick = { currentPage = it },
                        icon = it.icon,
                        title = it.title,
                    )
                }
            }
            val pageModifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(myColors.background)
                .padding(16.dp)
            when (currentPage) {
                QueueInfoPages.Config -> RenderQueueConfig(pageModifier, component)
                QueueInfoPages.Items -> RenderQueueItems(pageModifier, component)
            }
        }
    }
}

@Composable
fun RenderQueueItems(
    modifier: Modifier,
    component: QueueInfoComponent,
) {
    val windowInfo = LocalWindowInfo.current
    fun isCtrlPressed() = windowInfo.keyboardModifiers.isCtrlPressed
    val queueModel by component.downloadQueue.queueModel.collectAsState()
    val downloadItems by component.downloadQueueItems.collectAsState()
    val selectedIds by component.selectedListItems.collectAsState()
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            component.swapItem(from.index, to.index)
        }
    )

    Column(modifier) {
        LazyColumn(
            state = state.listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .reorderable(state)
        ) {
            itemsIndexed(downloadItems,
                key = { _, item -> item.id }
            ) { index, downloadItem ->
                RenderQueueItem(
                    state = state,
                    value = downloadItem,
                    isSelected = selectedIds.contains(downloadItem.id),
                    setSelected = { selected ->
                        component.setSelectedItem(
                            id = downloadItem.id,
                            selected = selected,
                            singleSelect = !isCtrlPressed()
                        )
                    },
                    index = index
                )
            }
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 5)
        )
        Row(Modifier.padding(8.dp)) {
            val hasSelections = selectedIds.isNotEmpty()
            val space = 4.dp
            IconActionButton(
                icon = MyIcons.remove,
                contentDescription = myStringResource(Res.string.remove),
                onClick = {
                    component.deleteItems()
                },
                enabled = hasSelections,
            )
            Spacer(Modifier.weight(1f))
            IconActionButton(
                icon = MyIcons.down,
                contentDescription = myStringResource(Res.string.move_down),
                onClick = {
                    component.moveDownItems()
                },
                enabled = hasSelections,
            )
            Spacer(Modifier.width(space))
            IconActionButton(
                icon = MyIcons.up,
                contentDescription = myStringResource(Res.string.move_up),
                onClick = {
                    component.moveUpItems()
                },
                enabled = hasSelections,
            )
        }
    }
}

@Composable
private fun LazyItemScope.RenderQueueItem(
    state: ReorderableLazyListState,
    value: IDownloadItemState,
    isSelected: Boolean,
    setSelected: (Boolean) -> Unit,
    index: Int
) {
    ReorderableItem(
        state, key = value.id
    ) { dragging ->
        Box(
            modifier = Modifier
                .background(
                    animateColorAsState(
                        if (dragging) {
                            myColors.onBackground / 10
                        } else {
                            Color.Transparent
                        }
                    ).value
                )
                .detectReorder(state)
        ) {
            NavigateableItem(
                isSelected = isSelected,
                onClick = {
                    setSelected(!isSelected)
                },
                content = {
                    val isActive = if (value.statusOrFinished() is DownloadJobStatus.IsActive) {
                        true
                    } else {
                        false
                    }
                    Row {
                        Text(
                            "${index + 1}. ",
                            fontSize = myTextSizes.base,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            color = (if (isActive) {
                                myColors.success
                            } else {
                                LocalContentColor.current
                            }) / LocalContentAlpha.current,
                            modifier = Modifier
                                .border(1.dp, myColors.onBackground / 5)
                                .padding(1.dp)
                        )
                        Text(
                            value.name,
                            fontSize = myTextSizes.base,
                            maxLines = 1,
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RenderQueueConfig(
    modifier: Modifier,
    component: QueueInfoComponent,
) {
    val configurables: List<ConfigurableGroup> = component.configurations
    Column(
        modifier
            .verticalScroll(rememberScrollState())
    ) {
        for ((index, cfgGroup) in configurables.withIndex()) {
            RenderConfigurableGroup(
                cfgGroup,
                Modifier
            )
            if (index != configurables.lastIndex) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun QueueListSection(
    modifier: Modifier,
    queues: List<DownloadQueue>,
    selectedItem: Long,
    setSelected: (Long) -> Unit,
    component: QueuesComponent,
) {
    Column(modifier) {
        Column(
            Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            for (s in queues) {
                val queueModel by s.queueModel.collectAsState()
                val isQueueActive by s.activeFlow.collectAsState()
                val isSelected = selectedItem == s.id
                NavigateableItem(
                    isSelected = isSelected,
                    onClick = { setSelected(s.id) }
                ) {
                    MyIcon(
                        MyIcons.folder,
                        null,
                        Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        queueModel.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Spacer(Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isQueueActive) {
                                myColors.success
                            } else {
                                myColors.onSurface / 50
                            }
                        )
                    )
                }
            }
        }
        val spacer = @Composable { Spacer(Modifier.width(4.dp)) }
        Spacer(
            Modifier
                .background(myColors.onBackground / 5)
                .fillMaxWidth()
                .height(1.dp)
        )
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconActionButton(
                icon = MyIcons.add,
                contentDescription = myStringResource(Res.string.add_new_queue),
                onClick = {
                    component.addQueue()
                }
            )
            spacer()
            IconActionButton(
                icon = MyIcons.remove,
                contentDescription = myStringResource(Res.string.remove_queue),
                enabled = component.canDeleteThisQueue(selectedItem),
                onClick = {
                    component.requestDeleteQueue(selectedItem)
                }
            )
        }
    }
}
