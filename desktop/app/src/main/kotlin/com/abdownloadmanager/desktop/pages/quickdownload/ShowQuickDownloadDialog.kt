package com.abdownloadmanager.desktop.pages.quickdownload

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowIcon
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.desktop.PlatformAppActivator
import java.awt.Dimension

@Composable
fun ShowQuickDownloadDialog(
    component: QuickDownloadComponent,
    onRequestClose: () -> Unit,
) {
    val w = 420
    val h = 230
    val size = DpSize(width = w.dp, height = h.dp)

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
            PlatformAppActivator.active()
        }
        WindowTitle("Quick Download")
        WindowIcon(MyIcons.appIcon)
        QuickDownloadPage(component)
    }
}
