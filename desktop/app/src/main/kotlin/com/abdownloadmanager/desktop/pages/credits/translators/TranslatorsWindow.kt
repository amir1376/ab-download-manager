package com.abdownloadmanager.desktop.pages.credits.translators

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.resources.myStringResource


@Composable
fun ShowTranslators(
    appComponent: AppComponent,
) {
    TranslatorsWindow(
        isVisible = appComponent.showTranslators.collectAsState().value,
        onRequestClose = {
            appComponent.closeTranslatorsPage()
        }
    )
}

@Composable
private fun TranslatorsWindow(
    isVisible: Boolean,
    onRequestClose: () -> Unit,
) {
    if (!isVisible) return
    CustomWindow(
        onCloseRequest = onRequestClose,
        state = rememberWindowState(
            size = DpSize(650.dp, 500.dp)
        )
    ) {
        WindowTitle(myStringResource(Res.string.meet_the_translators))
        Translators(
            modifier = Modifier.fillMaxSize(),
        )
    }
}