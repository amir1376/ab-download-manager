package com.abdownloadmanager.desktop.pages.about

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.ui.customwindow.WindowIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.LocalUiScale
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun ShowAboutDialog(appComponent: AppComponent) {
    if (appComponent.showAboutPage.collectAsState().value) {
        AboutDialog(
            onClose = {
                appComponent.closeAbout()
            },
            onRequestShowOpenSourceLibraries = {
                appComponent.openOpenSourceLibraries()
            },
            onRequestShowTranslators = {
                appComponent.openTranslatorsPage()
            }
        )
    }
}

@Composable
fun AboutDialog(
    onClose: () -> Unit,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    CustomWindow(
        resizable = false,
        onRequestToggleMaximize = null,
        alwaysOnTop = false,
        onRequestMinimize = null,
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(600.dp, 310.dp)
                .applyUiScale(LocalUiScale.current)
        ),
        onCloseRequest = onClose
    ) {
        WindowTitle(myStringResource(Res.string.about))
        WindowIcon(MyIcons.info)
        AboutPage(
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
            onRequestShowTranslators = onRequestShowTranslators
        )
    }
}