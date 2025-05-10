package com.abdownloadmanager.desktop.window.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

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
