package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun ShowOpenSourceLibraries(appComponent: AppComponent) {
    ShowOpenSourceLibraries(
        visible = appComponent.showOpenSourceLibraries.collectAsState().value,
        onRequestClose = {
            appComponent.closeOpenSourceLibraries()
        }
    )
}

@Composable
fun ShowOpenSourceLibraries(
    visible: Boolean,
    onRequestClose: () -> Unit,
) {
    if (!visible) return
    CustomWindow(
        onCloseRequest = onRequestClose,
        state = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                sizeProvider = WindowSizeProvider.Fixed(
                    size = DpSize(650.dp, 400.dp)
                        .applyUiScale(LocalUiScale.current)
                ),
            )
        )
    ) {
        WindowTitle(myStringResource(Res.string.open_source_software_used_in_this_app))
        ExternalLibsPage()
    }
}