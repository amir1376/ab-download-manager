package com.abdownloadmanager.shared.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

@Composable
actual fun createPlatformSettings(attachToWindow: Boolean): FileKitPlatformSettings {
    return FileKitPlatformSettings()
}
