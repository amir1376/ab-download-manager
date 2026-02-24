package com.abdownloadmanager.android.pages.home.sections.queues

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.abdownloadmanager.android.pages.home.HomeComponent
import com.abdownloadmanager.android.ui.menu.RenderMenuInSinglePage
import com.abdownloadmanager.android.ui.myCombinedClickable
import com.abdownloadmanager.shared.pages.home.queue.QueueActions
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.rememberMyPopupPositionProviderAtPosition
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentAlpha
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
internal fun QueuesSection(
    component: HomeComponent,
    modifier: Modifier,
) {

    val currentSelectedQueue = component.filterState.queueFilter
    val filterMode by component.filterMode
    val queues by component.queueManager.queues.collectAsState()
    val clipShape = myShapes.defaultRounded
    val showQueueOption by component.queueActions.collectAsState()
    var lastPointerPosition by remember { mutableStateOf(Offset.Zero) }
    fun showQueueOption(downloadQueue: DownloadQueue?, pointerPosition: Offset) {
        lastPointerPosition = pointerPosition
        component.showCategoryOptions(downloadQueue)
    }

    fun closeQueueOptions() {
        component.closeQueueOptions()
    }

    var isExpanded by remember {
        mutableStateOf(
            filterMode is HomeComponent.FilterMode.Queue
        )
    }
    Column(
        modifier
            .border(1.dp, myColors.surface, clipShape)
            .clip(clipShape)
            .padding(1.dp),
    ) {
        var layoutCoordinates by remember {
            mutableStateOf(null as LayoutCoordinates?)
        }
        ExpandableItem(
            isExpanded = isExpanded,
            modifier = Modifier,
            header = {
                Box(
                    Modifier
                        .height(IntrinsicSize.Max)
                        .heightIn(mySpacings.thumbSize)
                        .onGloballyPositioned {
                            layoutCoordinates = it
                        }
                        .myCombinedClickable(
                            onClick = {
                                isExpanded = !isExpanded
                            },
                            onLongClick = {
                                showQueueOption(null, layoutCoordinates?.localToWindow(it) ?: Offset.Zero)
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current,
                        )
                ) {
                    Row(
                        Modifier
                            .padding(vertical = 4.dp)
                            .padding(start = 16.dp)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WithContentAlpha(0.75f) {
                            MyIcon(
                                MyIcons.queue,
                                null,
                                Modifier.size(mySpacings.iconSize)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                myStringResource(Res.string.queues),
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Normal,
                                fontSize = myTextSizes.lg,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            MyIcon(
                                MyIcons.up, null, Modifier
                                    .fillMaxHeight()
                                    .wrapContentHeight()
                                    .clip(CircleShape)
                                    .clickable {
                                        isExpanded = !isExpanded
                                    }
                                    .padding(6.dp)
                                    .size(16.dp)
                                    .rotate(if (isExpanded) 0f else 180f))
                        }
                    }
                }
            },
            body = {
                Column {
                    queues.forEachIndexed { index, queue ->
                        key(queue.id) {
                            QueueFilterItem(
                                modifier = Modifier,
                                isSelected = currentSelectedQueue?.id == queue.id,
                                onSelect = {
                                    component.onQueueFilterChange(queue.queueModel.value)
                                },
//                                onItemsDroppedInQueue = { downloadIds ->
//                                    component.moveItemsToQueue(queue, downloadIds)
//                                },
                                queueModel = queue.queueModel.collectAsState().value,
                                isActive = queue.activeFlow.collectAsState().value,
                                showQueueOption = { position ->
                                    showQueueOption(queue, position)
                                }
//                                parentShape = clipShape,
//                                isLast = queues.lastIndex == index
                            )
                        }
                    }
                }
            },
        )
    }
    showQueueOption?.let {
        QueueOption(
            queueOptionMenuState = it,
            onDismiss = {
                closeQueueOptions()
            },
            position = lastPointerPosition,
        )
    }
}

@Composable
private fun QueueFilterItem(
    isSelected: Boolean,
    onSelect: () -> Unit,
//    onItemsDroppedInQueue: (List<Long>) -> Unit,
    queueModel: QueueModel,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    showQueueOption: (offset: Offset) -> Unit,
    // I add this to properly create border on drag when the item is in the last position
//    isLast: Boolean,
//    parentShape: RoundedCornerShape,
) {
//    var isDraggingOnMe by remember { mutableStateOf(false) }
    var layoutCoordinates by remember { mutableStateOf(null as LayoutCoordinates?) }
    Box(
        modifier
//            .dropDownloadItemsHere(
//                onDragIn = { isDraggingOnMe = true },
//                onDragDone = { isDraggingOnMe = false },
//                onItemsDropped = onItemsDroppedInQueue,
//            )
            .background(
                if (isSelected) {
                    myColors.onBackground / 0.05f
                } else Color.Transparent
            )
//            .ifThen(isDraggingOnMe) {
//                val infiniteTransition = rememberInfiniteTransition()
//                val color by infiniteTransition.animateColor(
//                    initialValue = myColors.primary,
//                    targetValue = myColors.secondary,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(1000, easing = LinearEasing),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                val shape = RoundedCornerShape(0.dp).let {
//                    when {
//                        isLast -> it.copy(
//                            bottomStart = parentShape.bottomStart,
//                            bottomEnd = parentShape.bottomEnd,
//                        )
//
//                        else -> it
//                    }
//                }
//                border(1.dp, color, shape)
//            }
            .onGloballyPositioned {
                layoutCoordinates = it
            }
            .myCombinedClickable(
                onClick = {
                    onSelect()
                },
                onLongClick = {
                    showQueueOption(layoutCoordinates?.localToWindow(it) ?: Offset.Zero)
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
            )
    ) {
//        if (isDraggingOnMe) {
//            DelayedTooltipPopup(
//                {},
//                myStringResource(Res.string.move_to_this_queue),
//            )
//        }
        Row(
            Modifier
                .heightIn(mySpacings.thumbSize)
                .padding(start = 24.dp)
                .padding(end = 8.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                MyIcon(
                    MyIcons.folder,
                    null,
                    Modifier.size(mySpacings.iconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    queueModel.name,
                    Modifier.weight(1f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = myTextSizes.lg,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                val counterColor = animateColorAsState(
                    if (isActive) {
                        myColors.success
                    } else {
                        LocalContentColor.current / LocalContentAlpha.current
                    }
                ).value
                Text(
                    text = "${queueModel.queueItems.size}",
                    modifier = Modifier.padding(horizontal = 6.dp),
                    color = counterColor
                )
            }
        }
        AnimatedVisibility(
            isSelected,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = scaleIn(),
            exit = scaleOut(),
        ) {
            Spacer(
                Modifier
                    .height(16.dp)
                    .width(3.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 12.dp,
                            topEnd = 12.dp,
                        )
                    )
                    .background(myColors.primary)
            )
        }
    }
}

@Composable
private fun QueueOption(
    queueOptionMenuState: QueueActions,
    onDismiss: () -> Unit,
    position: Offset,
) {
    ShowOptionsInPopup(
        MenuItem.SubMenu(
            icon = MyIcons.queue,
            title = queueOptionMenuState.mainQueueModel?.name?.asStringSource() ?: Res.string.queues.asStringSource(),
            items = queueOptionMenuState.menu,
        ),
        onDismiss,
        position,
    )
}

@Composable
private fun ShowOptionsInPopup(
    subMenu: MenuItem.SubMenu,
    onDismiss: () -> Unit,
    position: Offset,
) {
    Popup(
        popupPositionProvider = rememberMyPopupPositionProviderAtPosition(position),
        onDismissRequest = onDismiss,
    ) {
        RenderMenuInSinglePage(
            menu = subMenu,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(IntrinsicSize.Max)
        )
    }
}
