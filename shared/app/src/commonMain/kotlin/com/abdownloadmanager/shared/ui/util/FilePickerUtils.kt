package com.abdownloadmanager.shared.ui.util

import androidx.compose.runtime.Composable
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
expect fun createPlatformSettings(
    attachToWindow: Boolean
): FileKitPlatformSettings
