package com.abdownloadmanager.desktop.pages.addDownload.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.desktop.actions.newQueueAction
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.desktop.window.moveSafe
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.adddownload.addToQueue.SelectQueueComponent
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.MultiplatformVerticalScrollbar
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale
import ir.amirab.util.ifThen
import java.awt.MouseInfo

@Composable
fun ShowAddToQueueDialog(
    queueComponent: SelectQueueComponent,
) {
    if (queueComponent.shouldShowAddToQueue) {
        ShowAddToQueueDialog(
            queueList = queueComponent.queueList.collectAsState().value,
            selectedQueue = queueComponent.selectedQueue.collectAsState().value,
            onQueueSelected = queueComponent::setSelectedQueue,
            startQueue = queueComponent.startQueue.collectAsState().value,
            setStartQueue = queueComponent::setStartQueue,
            rememberThisChoice = queueComponent.rememberThisChoice.collectAsState().value,
            setRememberThisChoice = queueComponent::setRememberThisChoice,
            onClose = queueComponent::closeAddToQueue,
            onConfirm = queueComponent::onConfirm,
        )
    }
}

@Composable
private fun ShowAddToQueueDialog(
    queueList: List<DownloadQueue>,
    selectedQueue: Long?,
    onQueueSelected: (Long?) -> Unit,
    startQueue: Boolean,
    setStartQueue: (Boolean) -> Unit,
    rememberThisChoice: Boolean,
    setRememberThisChoice: (Boolean) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    val h = 210
    val w = 250
    val state = rememberDialogState(
        size = DpSize(
            height = h.dp,
            width = w.dp,
        ).applyUiScale(LocalUiScale.current),
    )
    val close = {
        onClose()
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


        val shape = myShapes.defaultRounded
        val withoutQueueSelected = selectedQueue == null
        Column(
            Modifier
                .fillMaxSize()
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
                // needs improvements: extract this layout for reuse
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    WindowDraggableArea(Modifier.fillMaxWidth()) {
                        Text(
                            myStringResource(Res.string.select_queue),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            fontSize = myTextSizes.lg,
                        )
                    }
                    Divider()
                    Column(
                        Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        val addToQueueModifier = Modifier.fillMaxWidth()
                        Box(
                            Modifier
                                .weight(1f)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                            ) {
                                QueueItemToSelect(
                                    modifier = addToQueueModifier,
                                    isSelected = selectedQueue == null,
                                    onSelect = {
                                        onQueueSelected(null)
                                    },
                                    name = myStringResource(Res.string.without_queue),
                                )
                                for (q in queueList) {
                                    key(q.id) {
                                        val queueModel by q.queueModel.collectAsState()
                                        QueueItemToSelect(
                                            modifier = addToQueueModifier,
                                            name = queueModel.name,
                                            isSelected = selectedQueue == q.id,
                                            onSelect = {
                                                onQueueSelected(queueModel.id)
                                            }
                                        )
                                    }
                                }
                            }
                            MultiplatformVerticalScrollbar(
                                rememberScrollbarAdapter(scrollState),
                                Modifier.fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                            )
                        }
                        Divider()
                        Column(
                            Modifier
                                .padding(vertical = 4.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            FlowRow(
                                itemVerticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                LabeledCheckbox(
                                    modifier = Modifier,
                                    value = startQueue,
                                    onValueChange = setStartQueue,
                                    enabled = !withoutQueueSelected,
                                    description = myStringResource(Res.string.start_queue),
                                )
                                LabeledCheckbox(
                                    modifier = Modifier,
                                    value = rememberThisChoice,
                                    onValueChange = setRememberThisChoice,
                                    description = myStringResource(Res.string.remember_this),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconActionButton(
                                    icon = MyIcons.add,
                                    contentDescription = Res.string.add_new_queue.asStringSource(),
                                    onClick = newQueueAction,
                                )
                                Spacer(Modifier.width(4.dp))
                                PrimaryMainActionButton(
                                    onClick = onConfirm,
                                    text = myStringResource(Res.string.ok),
                                    modifier = Modifier.fillMaxWidth(),
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
private fun QueueItemToSelect(
    modifier: Modifier,
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier
            .ifThen(isSelected) {
                background(myColors.selectionGradient())
            }
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
            .padding(horizontal = 8.dp)
    ) {
        RadioButton(
            isSelected,
            onValueChange = {
                if (it) {
                    onSelect()
                }
            },
        )
        Spacer(Modifier.width(mySpacings.mediumSpace))
        Text(
            name,
            fontSize = myTextSizes.base,
        )
    }
}

@Composable
private fun Divider() {
    Spacer(
        Modifier.fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10),
    )
}
