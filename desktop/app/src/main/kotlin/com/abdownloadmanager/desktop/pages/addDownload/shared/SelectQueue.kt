package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.desktop.actions.newQueueAction
import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.IconActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.desktop.utils.windowUtil.moveSafe
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
import ir.amirab.downloader.queue.DownloadQueue
import java.awt.MouseInfo

@Composable
fun ShowAddToQueueDialog(
    queueList: List<DownloadQueue>,
    onQueueSelected: (Long?) -> Unit,
    onClose: () -> Unit,
) {
    val h = 210
    val w = 250
    val state = rememberDialogState(
        size = DpSize(
            height = h.dp,
            width = w.dp,
        ),
    )
    val close = {
        onClose()
    }
    BaseOptionDialog(
        onCloseRequest = close,
        state = state,
        resizeable = false,
    ) {
        LaunchedEffect(window){
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
                            "Choose Queue to add",
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
                                                onQueueSelected(queueModel.id)
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
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            IconActionButton(
                                MyIcons.add,
                                contentDescription = "Add new queue",
                                onClick = newQueueAction
                            )
                            ActionButton(
                                text = "Without Queue",
                                modifier = Modifier,
                                onClick = {
                                    onQueueSelected(null)
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
    Row(modifier
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
