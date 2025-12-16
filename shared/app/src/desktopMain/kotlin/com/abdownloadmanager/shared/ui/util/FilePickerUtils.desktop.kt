package com.abdownloadmanager.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PlatformDirectory

@Composable
actual fun createPlatformSettings(attachToWindow: Boolean): FileKitPlatformSettings {
    return FileKitPlatformSettings(
        parentWindow = LocalWindow.current.takeIf { attachToWindow }
    )
}
