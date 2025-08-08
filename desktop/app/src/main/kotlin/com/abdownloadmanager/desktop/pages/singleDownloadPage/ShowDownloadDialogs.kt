package com.abdownloadmanager.desktop.pages.singleDownloadPage

import com.abdownloadmanager.desktop.DownloadDialogManager
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowIcon
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.mvi.HandleEffects
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.shared.utils.ui.theme.LocalUiScale
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.desktop.screen.applyUiScale
import java.awt.Dimension
import java.awt.Taskbar
import java.awt.Window

@Composable
private fun getDownloadTitle(itemState: IDownloadItemState): String {
    return buildString {
        if (itemState is ProcessingDownloadItemState && itemState.percent != null) {
            append("${itemState.percent}%")
            append(" ")
        }
        append(itemState.name)
    }
}

val LocalSingleDownloadPageSizing =
    compositionLocalOf<SingleProgressDownloadPageSizing> { error("LocalSingleBoxSizing not provided") }

@Stable
class SingleProgressDownloadPageSizing {
    var resizingPartInfo by mutableStateOf(false)
    var partInfoHeight by mutableStateOf(150.dp)
}

@Composable
fun ShowDownloadDialogs(component: DownloadDialogManager) {
    val openedDownloadDialogs = component.openedDownloadDialogs.collectAsState().value
    for (singleDownloadComponent in openedDownloadDialogs) {
        key(singleDownloadComponent.downloadId) {
            ShowDownloadDialog(singleDownloadComponent)
        }
    }
}

@Composable
private fun ShowDownloadDialog(singleDownloadComponent: SingleDownloadComponent) {
    val itemState by singleDownloadComponent.itemStateFlow.collectAsState()
    itemState?.let {
        when (it) {
            is CompletedDownloadItemState -> {
                CompletedWindow(
                    singleDownloadComponent,
                    it,
                )
            }

            is ProcessingDownloadItemState -> {
                ProgressWindow(
                    singleDownloadComponent = singleDownloadComponent,
                    itemState = it,
                )
            }
        }
    }
}


@Composable
private fun FrameWindowScope.CommonContent(
    singleDownloadComponent: SingleDownloadComponent,
    state: WindowState,
    itemState: IDownloadItemState,
) {
    HandleEffects(singleDownloadComponent) {
        when (it) {
            SingleDownloadEffects.BringToFront -> {
                state.isMinimized = false
                window.toFront()
            }
        }
    }
    WindowTitle(getDownloadTitle(itemState))
    WindowIcon(MyIcons.appIcon)
    UpdateTaskBar(window, itemState)
}

@Composable
private fun CompletedWindow(
    singleDownloadComponent: SingleDownloadComponent,
    itemState: CompletedDownloadItemState,
) {
    val onRequestClose = {
        singleDownloadComponent.close()
    }
    val defaultHeight = 160f
    val defaultWidth = 450f
    val uiScale = LocalUiScale.current
    val state = rememberWindowState(
        size = DpSize(
            height = defaultHeight.dp,
            width = defaultWidth.dp
        ).applyUiScale(uiScale),
        position = WindowPosition(Alignment.Center)
    )
    CustomWindow(
        state = state,
        onRequestToggleMaximize = null,
        resizable = false,
        alwaysOnTop = true,
        onCloseRequest = onRequestClose,
    ) {
        CommonContent(
            singleDownloadComponent = singleDownloadComponent,
            state = state,
            itemState = itemState,
        )
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(defaultWidth.toInt(), defaultHeight.toInt())
        }
        var h = defaultHeight
        var w = defaultWidth
        LaunchedEffect(w, h) {
            state.size = DpSize(
                width = w.dp,
                height = h.dp
            ).applyUiScale(uiScale)
        }
        CompletedDownloadPage(
            singleDownloadComponent,
            itemState,
        )
    }
}

@Composable
private fun ProgressWindow(
    singleDownloadComponent: SingleDownloadComponent,
    itemState: ProcessingDownloadItemState,
) {
    val onRequestClose = {
        singleDownloadComponent.close()
    }
    val uiScale = LocalUiScale.current
    val defaultHeight = 290f.applyUiScale(uiScale)
    val defaultWidth = 450f.applyUiScale(uiScale)

    val showPartInfo by singleDownloadComponent.showPartInfo.collectAsState()
    val singleDownloadPageSizing = remember(showPartInfo) { SingleProgressDownloadPageSizing() }
    var h = defaultHeight
    var w = defaultWidth
    if (showPartInfo) {
        h += singleDownloadPageSizing.partInfoHeight.value
            .applyUiScale(uiScale)
    }
    val state = rememberWindowState(
        height = h.dp,
        width = w.dp,
        position = WindowPosition(Alignment.Center)
    )
    CustomWindow(
        state = state,
        onRequestToggleMaximize = null,
        resizable = false,
        onCloseRequest = onRequestClose,
    ) {
        CommonContent(
            singleDownloadComponent = singleDownloadComponent,
            state = state,
            itemState = itemState,
        )
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(defaultWidth.toInt(), defaultHeight.toInt())
        }
        LaunchedEffect(w, h) {
            state.size = DpSize(
                width = w.dp,
                height = h.dp
            )
        }
        CompositionLocalProvider(
            LocalSingleDownloadPageSizing provides singleDownloadPageSizing
        ) {
            ProgressDownloadPage(
                singleDownloadComponent,
                itemState,
            )
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

                    DownloadJobStatus.Downloading,
                    is DownloadJobStatus.Retrying -> {
                        if (percent != null) {
                            Taskbar.State.NORMAL
                        } else {
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
