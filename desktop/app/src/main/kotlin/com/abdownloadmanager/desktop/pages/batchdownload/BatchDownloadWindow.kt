package com.abdownloadmanager.desktop.pages.batchdownload

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.pages.batchdownload.BaseBatchDownloadComponent
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun BatchDownloadWindow(appComponent: AppComponent) {
    appComponent.batchDownloadSlot.rememberChild()?.let {
        BatchDownloadWindow(it)
    }
}

@Composable
private fun BatchDownloadWindow(desktopBatchDownloadComponent: DesktopBatchDownloadComponent) {
    CustomWindow(
        state = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                sizeProvider = WindowSizeProvider.Fixed(
                    size = DpSize(500.dp, 420.dp)
                        .applyUiScale(LocalUiScale.current),
                ),
                positionProvider = WindowPositionProvider.CenteredOnScreen
            )
        ),
        onCloseRequest = desktopBatchDownloadComponent.onClose
    ) {
        HandleEffects(desktopBatchDownloadComponent) {
            when (it) {
                DesktopBatchDownloadComponent.Effects.BringToFront -> window.toFront()
                is BaseBatchDownloadComponent.Effects.PlatformEffects -> {
                    //
                }
            }
        }
        BatchDownload(desktopBatchDownloadComponent)
    }
}
