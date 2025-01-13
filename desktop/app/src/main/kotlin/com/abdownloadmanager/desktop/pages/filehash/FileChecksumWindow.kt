package com.abdownloadmanager.desktop.pages.filehash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.utils.ui.theme.LocalUiScale
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun FileChecksumWindow(
    component: AppComponent
) {
    component.openedFileChecksumDialog.collectAsState().value.child?.instance?.let {
        FileChecksumWindow(it)
    }
}

@Composable
fun FileChecksumWindow(
    component: FileChecksumComponent
) {
    val uiScale = LocalUiScale.current
    CustomWindow(
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(900.dp, 400.dp).applyUiScale(uiScale)
        ),
        onCloseRequest = component::onRequestClose
    ) {
        FileChecksumPage(component)
    }
}
