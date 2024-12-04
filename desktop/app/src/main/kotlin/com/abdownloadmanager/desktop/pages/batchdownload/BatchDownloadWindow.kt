package com.abdownloadmanager.desktop.pages.batchdownload

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.theme.LocalUiScale
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun BatchDownloadWindow(batchDownloadComponent: BatchDownloadComponent) {
    CustomWindow(
        state = rememberWindowState(
            size = DpSize(500.dp, 420.dp)
                .applyUiScale(LocalUiScale.current),
            position = WindowPosition(Alignment.Center)
        ),
        onCloseRequest = batchDownloadComponent.onClose
    ) {
        HandleEffects(batchDownloadComponent) {
            when (it) {
                BatchDownloadEffects.BringToFront -> window.toFront()
            }
        }
        BatchDownload(batchDownloadComponent)
    }
}