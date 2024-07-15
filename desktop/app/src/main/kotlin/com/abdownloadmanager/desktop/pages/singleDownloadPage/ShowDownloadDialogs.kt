package com.abdownloadmanager.desktop.pages.singleDownloadPage

import com.abdownloadmanager.desktop.DownloadDialogManager
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowIcon
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.utils.ExceptionUtils
import java.awt.Dimension
import java.awt.Taskbar
import java.awt.Window

//@Composable
//fun DownloadDialogTitle(itemState: IDownloadItemState) {
//    Row {
//        if (itemState is ProcessingDownloadItemState) {
//            Text("${itemState.percent}%")
//            Text(" | ")
//        }
//        Text("${itemState.name}", Modifier.basicMarquee(iterations = Int.MAX_VALUE), maxLines = 1)
//    }
//}
@Composable
fun getDownloadTitle(itemState: IDownloadItemState): String {
    return buildString {
        if (itemState is ProcessingDownloadItemState && itemState.percent!=null) {
            append("${itemState.percent}%")
            append("-")
        }
        append("${itemState.name}")
    }
}

val LocalSingleBoxSizing = compositionLocalOf<SingleDownloadPageSizing> { error("LocalSingleBoxSizing not provided") }

@Stable
class SingleDownloadPageSizing {
    var partInfoHeight by mutableStateOf(150.dp)
}

@Composable
fun ShowDownloadDialogs(component: DownloadDialogManager) {
    val openedDownloadDialogs = component.openedDownloadDialogs.collectAsState().value
    for (singleDownloadComponent in openedDownloadDialogs) {
        val onRequestClose = {
            component.closeDownloadDialog(singleDownloadComponent.downloadId)
        }
        val defaultHeight = 290f
        val defaultWidth = 450f

        val showPartInfo by singleDownloadComponent.showPartInfo
        val itemState by singleDownloadComponent.itemStateFlow.collectAsState()
        val state = rememberWindowState(
            height = defaultHeight.dp,
            width = defaultWidth.dp
        )
        CustomWindow(
            state = state,
            onRequestToggleMaximize = null,
            resizable = false,
            onCloseRequest = onRequestClose,
        ) {
            HandleEffects(singleDownloadComponent){
                when(it){
                    SingleDownloadEffects.BringToFront -> {
                        state.isMinimized=false
                        window.toFront()
                    }
                }
            }
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(defaultWidth.toInt(), defaultHeight.toInt())
            }
            val singleDownloadPageSizing = remember { SingleDownloadPageSizing() }
            WindowTitle(itemState?.let { getDownloadTitle(it) } ?: "Download")
            WindowIcon(MyIcons.appIcon)
            var h = defaultHeight
            var w = defaultWidth
            if (showPartInfo && itemState is ProcessingDownloadItemState) {
                h += singleDownloadPageSizing.partInfoHeight.value
            }
            state.size = DpSize(
                width = w.dp,
                height = h.dp
            )
            itemState?.let { itemState ->
                UpdateTaskBar(window, itemState)
            }
            CompositionLocalProvider(
                LocalSingleBoxSizing provides singleDownloadPageSizing
            ) {
                SingleDownloadPage(singleDownloadComponent)
            }
        }
    }
}

@Composable
private fun UpdateTaskBar(
    window: Window,
    state: IDownloadItemState,
) {
    val percent = state.getPercent()
    val status = state.statusOrFinished()
    LaunchedEffect(percent, status, window) {
        if (!Taskbar.isTaskbarSupported()) return@LaunchedEffect
        runCatching {
            val taskbar = Taskbar.getTaskbar()
            percent?.let {
                taskbar.setWindowProgressValue(
                    window,
                    percent
                )
            }
            taskbar.setWindowProgressState(
                window,
                when (status) {
                    is DownloadJobStatus.Canceled -> {
                        if (ExceptionUtils.isNormalCancellation(status.e)) {
                            Taskbar.State.PAUSED
                        } else {
                            Taskbar.State.ERROR
                        }
                    }

                    DownloadJobStatus.Downloading -> {
                        if (percent!=null){
                            Taskbar.State.NORMAL
                        }else{
                            Taskbar.State.INDETERMINATE
                        }
                    }

                    DownloadJobStatus.Resuming -> {
                        Taskbar.State.INDETERMINATE
                    }

                    DownloadJobStatus.Finished -> {
                        Taskbar.State.OFF
                    }

                    DownloadJobStatus.IDLE -> {
                        Taskbar.State.OFF
                    }

                    is DownloadJobStatus.PreparingFile -> {
                        Taskbar.State.INDETERMINATE
                    }
                }
            )
        }
    }
}


private fun IDownloadItemState.getPercent(): Int? {
    return when (this) {
        is CompletedDownloadItemState -> 100
        is ProcessingDownloadItemState -> percent
    }
}

private fun IDownloadItemState.isActive(): Boolean {
    return when (this) {
        is CompletedDownloadItemState -> false
        is ProcessingDownloadItemState -> status is DownloadJobStatus.IsActive
    }
}
