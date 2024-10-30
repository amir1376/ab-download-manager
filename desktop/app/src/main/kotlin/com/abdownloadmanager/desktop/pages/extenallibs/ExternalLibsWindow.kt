package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.resources.*
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun ShowOpenSourceLibraries(appComponent: AppComponent){
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
    onRequestClose:()->Unit,
) {
    if (!visible) return
    CustomWindow(
        onCloseRequest = onRequestClose,
        state = rememberWindowState(
            size = DpSize(650.dp, 400.dp)
        )
    ) {
        WindowTitle(myStringResource(Res.string.open_source_software_used_in_this_app))
        ExternalLibsPage()
    }
}