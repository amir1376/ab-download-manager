package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.desktop.actions.newQueueAction
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.desktop.window.moveSafe
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.abdownloadmanager.shared.utils.ui.theme.LocalUiScale
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.CheckBox
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.util.desktop.screen.applyUiScale
import java.awt.MouseInfo

@Composable
fun ShowAddToQueueDialog(
    queueList: List<DownloadQueue>,
    onQueueSelected: (Long?, Boolean) -> Unit,
    onClose: () -> Unit,
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
    val (startQueue, setStartQueue) = remember {
        mutableStateOf(false)
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
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        val addToQueueModifier = Modifier.fillMaxWidth()
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier
                                .border(1.dp, myColors.onBackground / 5, shape)
                                .padding(1.dp)
                                .weight(1f)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                            ) {
                                for (q in queueList) {
                                    key(q.id) {
                                        val queueModel by q.queueModel.collectAsState()
                                        QueueItemToSelect(
                                            modifier = addToQueueModifier,
                                            name = queueModel.name,
                                            onSelect = {
                                                onQueueSelected(queueModel.id, startQueue)
                                            }
                                        )
                                    }
                                }
                            }
                            VerticalScrollbar(
                                rememberScrollbarAdapter(scrollState),
                                Modifier.fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .onClick {
                                    setStartQueue(!startQueue)
                                }
                                .padding(vertical = 4.dp)
                                .padding(start = 2.dp)
                        ) {
                            CheckBox(
                                size = 14.dp,
                                value = startQueue,
                                onValueChange = setStartQueue
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(myStringResource(Res.string.start_queue))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconActionButton(
                                MyIcons.add,
                                contentDescription = myStringResource(Res.string.add_new_queue),
                                onClick = newQueueAction
                            )
                            ActionButton(
                                text = myStringResource(Res.string.without_queue),
                                modifier = Modifier,
                                onClick = {
                                    onQueueSelected(null, startQueue)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItemToSelect(
    modifier: Modifier,
    name: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            "$name",
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
