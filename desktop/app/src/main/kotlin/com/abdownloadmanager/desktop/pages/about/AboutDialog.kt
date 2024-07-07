package com.abdownloadmanager.desktop.pages.about

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.Ui
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState

@Composable
fun ShowAboutDialog(appComponent: AppComponent) {
    if (appComponent.showAboutPage.collectAsState().value) {
        AboutDialog(
            onClose = {
                appComponent.closeAbout()
            },
            onRequestShowOpenSourceLibraries = {
                appComponent.openOpenSourceLibraries()
            }
        )
    }
}

@Composable
fun AboutDialog(
    onClose: () -> Unit,
    onRequestShowOpenSourceLibraries: () -> Unit,
) {
    CustomWindow(
        resizable = false,
        onRequestToggleMaximize = null,
        state = rememberWindowState(
            size = DpSize(400.dp, 300.dp)
        ),
        onCloseRequest = onClose
    ) {
        WindowTitle("About")
        AboutPage(
            close = onClose,
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries
        )
    }
}