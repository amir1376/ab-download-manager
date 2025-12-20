package com.abdownloadmanager.desktop.ui.util

import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.ui.util.LocalWindow
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

@Composable
fun rememberMyDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    attachToWindow: Boolean = true,
    onResult: (String?) -> Unit,
): PickerResultLauncher {
    return rememberDirectoryPickerLauncher(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = createPlatformSettings(
            attachToWindow = attachToWindow
        ),
        onResult = {
            onResult(it?.path)
        },
    )
}

@Composable
fun createPlatformSettings(attachToWindow: Boolean): FileKitPlatformSettings {
    return FileKitPlatformSettings(
        parentWindow = LocalWindow.current.takeIf { attachToWindow }
    )
}
