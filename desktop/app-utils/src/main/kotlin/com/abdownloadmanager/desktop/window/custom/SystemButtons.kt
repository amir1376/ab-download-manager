package com.abdownloadmanager.desktop.window.custom

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

@Composable
internal fun FrameWindowScope.TitleBarContentWithSystemButtons(
    modifier: Modifier,
    onRequestMinimize: (() -> Unit)?,
    onRequestToggleMaximize: (() -> Unit)?,
    onRequestClose: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
        PlatformActionButtons(
            onRequestClose,
            onRequestMinimize,
            onRequestToggleMaximize,
        )
    }
}


@Composable
fun FrameWindowScope.PlatformActionButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
) {
    when (Platform.asDesktop()) {
        Platform.Desktop.Windows -> {
            WindowsSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onToggleMaximize
            )
        }

        Platform.Desktop.MacOS -> {
            MacOSSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onToggleMaximize
            )
        }

        Platform.Desktop.Linux -> {
            LinuxSystemButtons(
                onRequestClose = onRequestClose,
                onRequestMinimize = onRequestMinimize,
                onToggleMaximize = onToggleMaximize
            )
        }
    }
}
