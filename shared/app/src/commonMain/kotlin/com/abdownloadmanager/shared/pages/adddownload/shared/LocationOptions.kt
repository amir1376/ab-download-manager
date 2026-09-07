package com.abdownloadmanager.shared.pages.adddownload.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.resources.Strings.set_as_default_download_folder_for_createArgs
import ir.amirab.util.compose.resources.myStringResource

@Immutable
data class LastUsedLocationsProp(
    val locations: List<String>,
    val onRequestRemove: (String) -> Unit,
)

@Immutable
data class RememberFolderProp(
    val isEnabled: Boolean,
    val value: Boolean,
    val onValueChange: (Boolean) -> Unit,
    // null => default location
    val categoryName: String?,
)

@Composable
fun RememberFolderProp.rememberDescriptionString(): String {
    val rememberFolderProp = this
    val categoryName = rememberFolderProp.categoryName
    return if (categoryName == null) {
        myStringResource(Res.string.set_as_default_download_folder)
    } else {
        myStringResource(
            Res.string.set_as_default_download_folder_for,
            set_as_default_download_folder_for_createArgs(
                name = categoryName,
            )
        )
    }
}