package com.abdownloadmanager.android.pages.add.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.adddownload.addToQueue.SelectQueueComponent
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

@Composable
fun ShowAddToQueueDialog(
    queueComponent: SelectQueueComponent,
    onRequestAddNewQueue: () -> Unit,
) {
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
        isOpened = queueComponent.shouldShowAddToQueue,
        newQueueAction = onRequestAddNewQueue
    )
}

@Composable
private fun ShowAddToQueueDialog(
    queueList: List<DownloadQueue>,
    selectedQueue: Long?,
    onQueueSelected: (Long?) -> Unit,
    startQueue: Boolean,
    setStartQueue: (Boolean) -> Unit,
    newQueueAction: () -> Unit,
    rememberThisChoice: Boolean,
    setRememberThisChoice: (Boolean) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    isOpened: Boolean,
) {
    val withoutQueueSelected = selectedQueue == null
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(isOpened) {
        if (isOpened) {
            state.show()
        } else {
            state.hide()
        }
    }
    state.OnFullyDismissed {
        onClose()
    }
    ResponsiveDialog(
        onDismiss = state::hide,
        state = state,
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            myStringResource(Res.string.select_queue)
                        )
                    },
                    headerActions = {
                        TransparentIconActionButton(
                            icon = MyIcons.close,
                            contentDescription = Res.string.close.asStringSource(),
                            onClick = onClose
                        )
                    }
                )
            }
        ) {
            WithContentColor(myColors.onBackground) {
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                    ) {
                        val addToQueueModifier = Modifier.fillMaxWidth()
                        val scrollState = rememberScrollState()
                        VerticalScrollableContent(
                            scrollState,
                            Modifier
                                .padding(1.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                            ) {
                                QueueItemToSelect(
                                    modifier = addToQueueModifier,
                                    name = myStringResource(Res.string.without_queue),
                                    onSelect = {
                                        onQueueSelected(null)
                                    },
                                    isSelected = selectedQueue == null,
                                )
                                for (q in queueList) {
                                    key(q.id) {
                                        val queueModel by q.queueModel.collectAsState()
                                        QueueItemToSelect(
                                            modifier = addToQueueModifier,
                                            name = queueModel.name,
                                            onSelect = {
                                                onQueueSelected(queueModel.id)
                                            },
                                            isSelected = selectedQueue == queueModel.id,
                                        )
                                    }
                                }
                            }
                        }
                        Divider()
                        Column(
                            Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                itemVerticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconActionButton(
                                    icon = MyIcons.add,
                                    contentDescription = Res.string.add_new_queue.asStringSource(),
                                    onClick = newQueueAction
                                )
                                Spacer(Modifier.width(4.dp))
                                PrimaryMainActionButton(
                                    text = myStringResource(Res.string.ok),
                                    modifier = Modifier.weight(1f),
                                    onClick = onConfirm
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
            .heightIn(mySpacings.thumbSize)
            .padding(vertical = 4.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10),
    )
}
