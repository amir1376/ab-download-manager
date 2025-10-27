package com.abdownloadmanager.android.pages.directorypicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import ir.amirab.util.compose.StringSource
import okio.Path.Companion.toPath

class DirectoryPickerLauncher(
    private val onLaunch: () -> Unit,
) {
    fun launch() {
        onLaunch()
    }
}


@Composable
fun rememberAndroidDirectoryPickerLauncher(
    initialDirectory: String?,
    title: StringSource,
    onDirectorySelected: (String?) -> Unit,
): DirectoryPickerLauncher {
    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = DirectoryPickerActivity.Contract,
    ) { directory ->
        onDirectorySelected(directory?.toString())
    }
    val initialDirectory by rememberUpdatedState(initialDirectory)
    val title by rememberUpdatedState(title)
    return remember {
        DirectoryPickerLauncher {
            pickFolderLauncher.launch(
                DirectoryPickerActivity.Inputs(
                    title = title,
                    initialDirectory = initialDirectory?.toPath(),
                )
            )
        }
    }
}
