package com.abdownloadmanager.desktop.pages.home.sections.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.onClick
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.home.HomeComponent
import com.abdownloadmanager.desktop.pages.home.QueueActions
import com.abdownloadmanager.desktop.pages.home.dropDownloadItemsHere
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.DelayedTooltipPopup
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.menu.custom.ShowOptionsInDropDown
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.LocalContentAlpha
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

@Composable
internal fun QueuesSection(
    modifier: Modifier,
    component: HomeComponent,
) {

    val currentSelectedQueue = component.filterState.queueFilter
    val queues by component.queueManager.queues.collectAsState()
    val clipShape = RoundedCornerShape(6.dp)
    val showQueueOption by component.queueActions.collectAsState()

    fun showQueueOption(downloadQueue: DownloadQueue?) {
        component.showCategoryOptions(downloadQueue)
    }

    fun closeQueueOptions() {
        component.closeQueueOptions()
    }

    val (isExpanded, setExpanded) = remember { mutableStateOf(true) }
    Column(
        modifier
            .padding(start = 16.dp)
            .clip(clipShape)
            .border(1.dp, myColors.surface, clipShape)
            .padding(1.dp),
    ) {
        ExpandableItem(
            isExpanded = isExpanded,
            modifier = Modifier
                .onClick(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                ) {
                    showQueueOption(null)
                },
            header = {
                Box(
                    Modifier
                        .height(IntrinsicSize.Max)
                        .clickable(
                            onClick = {
                                setExpanded(!isExpanded)
                            }
                        )
                ) {
                    Row(
                        Modifier.padding(vertical = 4.dp)
                            .padding(start = 16.dp)
                            .padding(end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WithContentAlpha(0.75f) {
                            MyIcon(
                                MyIcons.queue,
                                null,
                                Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
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
                                    .fillMaxHeight().wrapContentHeight()
                                    .clip(CircleShape)
                                    .size(24.dp)
                                    .clickable {
                                        setExpanded(!isExpanded)
                                    }
                                    .padding(6.dp)
                                    .width(16.dp)
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
                                modifier = Modifier
                                    .onClick(
                                        matcher = PointerMatcher.mouse(PointerButton.Secondary),
                                    ) {
                                        showQueueOption(queue)
                                    },
                                isSelected = currentSelectedQueue?.id == queue.id,
                                onSelect = {
                                    component.onQueueFilterChange(queue.queueModel.value)
                                },
                                onItemsDroppedInQueue = { downloadIds ->
                                    component.moveItemsToQueue(queue, downloadIds)
                                },
                                queueModel = queue.queueModel.collectAsState().value,
                                isActive = queue.activeFlow.collectAsState().value,
                                parentShape = clipShape,
                                isLast = queues.lastIndex == index
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
            }
        )
    }
}

@Composable
private fun QueueFilterItem(
    isSelected: Boolean,
    onSelect: () -> Unit,
    onItemsDroppedInQueue: (List<Long>) -> Unit,
    queueModel: QueueModel,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    // I add this to properly create border on drag when the item is in the last position
    isLast: Boolean,
    parentShape: RoundedCornerShape,
) {
    var isDraggingOnMe by remember { mutableStateOf(false) }
    Box(
        modifier
            .dropDownloadItemsHere(
                onDragIn = { isDraggingOnMe = true },
                onDragDone = { isDraggingOnMe = false },
                onItemsDropped = onItemsDroppedInQueue,
            )
            .background(
                if (isSelected) {
                    myColors.onBackground / 0.05f
                } else Color.Transparent
            )
            .ifThen(isDraggingOnMe) {
                val infiniteTransition = rememberInfiniteTransition()
                val color by infiniteTransition.animateColor(
                    initialValue = myColors.primary,
                    targetValue = myColors.secondary,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                val shape = RoundedCornerShape(0.dp).let {
                    when {
                        isLast -> it.copy(
                            bottomStart = parentShape.bottomStart,
                            bottomEnd = parentShape.bottomEnd,
                        )

                        else -> it
                    }
                }
                border(1.dp, color, shape)
            }
            .selectable(
                selected = isSelected,
                onClick = {
                    onSelect()
                }
            )
    ) {
        if (isDraggingOnMe) {
            DelayedTooltipPopup(
                {},
                myStringResource(Res.string.move_to_this_queue),
            )
        }
        Row(
            Modifier
                .padding(start = 24.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                MyIcon(
                    MyIcons.folder,
                    null,
                    Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
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
) {
    ShowOptionsInDropDown(
        MenuItem.SubMenu(
            icon = MyIcons.queue,
            title = queueOptionMenuState.mainQueueModel?.name?.asStringSource() ?: Res.string.queues.asStringSource(),
            items = queueOptionMenuState.menu,
        ),
        onDismiss
    )
}
