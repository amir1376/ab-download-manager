package com.abdownloadmanager.desktop.ui.util

import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.ui.util.LocalWindow
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path

@Composable
fun rememberMyDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    attachToWindow: Boolean = true,
    onResult: (String?) -> Unit,
): PickerResultLauncher {
    return rememberDirectoryPickerLauncher(
        dialogSettings = createPlatformSettings(
            title = title,
            attachToWindow = attachToWindow,
        ),
        directory = initialDirectory?.let(::PlatformFile),
        onResult = {
            onResult(it?.path)
        },
    )
}

@Composable
fun rememberMyFilePickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    attachToWindow: Boolean = true,
    onResult: (String?) -> Unit,
    fileTypes: FileKitType = FileKitType.File()
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = fileTypes,
        onResult = {
            onResult(it?.path)
        },
        dialogSettings = createPlatformSettings(
            title = title,
            attachToWindow = attachToWindow,
        ),
        directory = initialDirectory?.let(::PlatformFile),
    )
}

@Composable
fun createPlatformSettings(
    attachToWindow: Boolean,
    title: String?,
): FileKitDialogSettings {
    val window = LocalWindow.current
    return FileKitDialogSettings(
        title = title,
        parent = if (attachToWindow) {
            FileKitDialogParent.awt(window)
        } else null
    )
}
