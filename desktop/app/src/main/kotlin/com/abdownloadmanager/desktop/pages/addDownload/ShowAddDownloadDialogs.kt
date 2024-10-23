package com.abdownloadmanager.desktop.pages.addDownload

import com.abdownloadmanager.desktop.AddDownloadDialogManager
import com.abdownloadmanager.desktop.pages.addDownload.multiple.AddMultiDownloadComponent
import com.abdownloadmanager.desktop.pages.addDownload.multiple.AddMultiItemPage
import com.abdownloadmanager.desktop.pages.addDownload.single.AddDownloadPage
import com.abdownloadmanager.desktop.pages.addDownload.single.AddSingleDownloadComponent
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadEffects
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowIcon
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.resources.*
import ir.amirab.util.compose.resources.myStringResource
import java.awt.Dimension

@Composable
fun ShowAddDownloadDialogs(component: AddDownloadDialogManager) {

    val openedAddDownloadDialogs = component.openedAddDownloadDialogs.collectAsState().value
    for (addDownloadComponent in openedAddDownloadDialogs) {
        val onRequestClose = {
            component.closeAddDownloadDialog(addDownloadComponent.id)
        }
        when (addDownloadComponent) {
            is AddSingleDownloadComponent -> {
                val h = 265
                val w = 500
                val size = remember {
                    DpSize(
                        height = h.dp,
                        width = w.dp,
                    )
                }

                val state = rememberWindowState(
                    size = size,
                    position = WindowPosition(Alignment.Center)
                )
                CustomWindow(
                    state = state,
                    onCloseRequest = onRequestClose,
                    alwaysOnTop = true,
                ) {
                    LaunchedEffect(Unit) {
                        window.minimumSize = Dimension(w, h)
                    }
//                    BringToFront()
                    WindowTitle(myStringResource(Res.string.add_download))
                    WindowIcon(MyIcons.appIcon)
                    AddDownloadPage(addDownloadComponent)
                }
            }

            is AddMultiDownloadComponent -> {
                val h = 450
                val w = 800
                val state = rememberWindowState(
                    height = h.dp,
                    width = w.dp,
                    position = WindowPosition(Alignment.Center)
                )
                CustomWindow(
                    state = state,
                    onCloseRequest = onRequestClose,
                    alwaysOnTop = true,
                ) {
                    LaunchedEffect(Unit) {
                        window.minimumSize = Dimension(w, h)
                    }
//                    BringToFront()
                    WindowTitle(myStringResource(Res.string.add_download))
                    WindowIcon(MyIcons.appIcon)
                    AddMultiItemPage(addDownloadComponent)
                }
            }
        }
    }
}

//it seems not affect at all
//@Composable
//private fun WindowScope.BringToFront() {
//    LaunchedEffect(Unit) {
//        window.toFront()
//        window.requestFocus()
//    }
//}