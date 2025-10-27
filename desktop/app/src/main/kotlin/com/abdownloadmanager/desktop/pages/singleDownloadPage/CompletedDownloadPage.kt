package com.abdownloadmanager.desktop.pages.singleDownloadPage

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.home.DownloadItemTransferable
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.LocalSizeUnit
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Tooltip
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.isShiftPressed

@Composable
fun CompletedDownloadPage(
    component: DesktopSingleDownloadComponent,
    completedDownloadItemState: CompletedDownloadItemState,
) {
    Column {
        Row(
            Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            RenderFileIconAndSize(
                modifier = Modifier.align(Alignment.CenterVertically),
                component = component,
                itemState = completedDownloadItemState,
            )
            Spacer(Modifier.width(16.dp))
            RenderName(
                Modifier.weight(1f),
                completedDownloadItemState.name,
            )
        }
        Spacer(Modifier.weight(1f))
        Actions(Modifier, component)
    }
}

@Composable
private fun Actions(
    modifier: Modifier,
    component: DesktopSingleDownloadComponent,
) {
    val iDownloadItemState by component.itemStateFlow.collectAsState()
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
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionButton(
                myStringResource(Res.string.open),
                modifier = Modifier,
                onClick = {
                    component.openFile()
                },
            )
            Spacer(Modifier.width(8.dp))
            ActionButton(
                myStringResource(Res.string.open_folder),
                modifier = Modifier,
                onClick = {
                    component.openFolder()
                },
            )
            Spacer(Modifier.width(8.dp))
            val dragTheFileDescription = Res.string.drag_the_file_to_another_app.asStringSource()
            val windowInfo = LocalWindowInfo.current
            Tooltip(dragTheFileDescription) {
                IconActionButton(
                    icon = MyIcons.dragAndDrop,
                    contentDescription = dragTheFileDescription.rememberString(),
                    modifier = Modifier
                        .dragAndDropSource(
                            drawDragDecoration = {},
                            transferData = {
                                val completedDownloadItemState =
                                    iDownloadItemState as? CompletedDownloadItemState
                                        ?: return@dragAndDropSource null

                                val shiftPressed = isShiftPressed(windowInfo)
                                val supportedActions = listOf(
                                    if (shiftPressed) {
                                        DragAndDropTransferAction.Move
                                    } else {
                                        DragAndDropTransferAction.Copy
                                    }
                                )
                                DragAndDropTransferData(
                                    transferable = DragAndDropTransferable(
                                        DownloadItemTransferable(
                                            listOf(completedDownloadItemState)
                                        )
                                    ),
                                    supportedActions = supportedActions,
                                )
                            }
                        ),
                    onClick = {},
                )
            }

            Spacer(Modifier.weight(1f))
            ActionButton(
                myStringResource(Res.string.close),
                modifier = Modifier,
                onClick = component::close,
            )
        }
    }
}

@Composable
private fun RenderName(
    modifier: Modifier,
    name: String,
) {
    Column(
        modifier = modifier
    ) {
        WithContentColor(
            myColors.success
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MyIcon(
                    MyIcons.check, null,
                    Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    myStringResource(Res.string.download_page_download_completed),
                    fontWeight = FontWeight.Bold,
                    fontSize = myTextSizes.lg,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = name,
            maxLines = 1,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE
            )
        )
    }
}

@Composable
private fun RenderFileIconAndSize(
    modifier: Modifier,
    component: DesktopSingleDownloadComponent,
    itemState: CompletedDownloadItemState,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MyIcon(
            icon = component.fileIconProvider.rememberIcon(itemState.name),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = convertPositiveSizeToHumanReadable(
                itemState.contentLength,
                LocalSizeUnit.current,
            ).rememberString(),
        )
    }
}
