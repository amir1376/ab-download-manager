package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.desktop.pages.addDownload.single.AddDownloadPageTextField
import com.abdownloadmanager.desktop.pages.addDownload.single.MyTextFieldIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.menu.custom.MyDropDown
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.ui.theme.myShapes
import ir.amirab.util.compose.resources.myStringResource
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import ir.amirab.util.desktop.LocalWindow
import java.io.File

@Composable
fun LocationTextField(
    modifier: Modifier,
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    lastUsedLocations: List<String> = emptyList(),
) {
    var showLastUsedLocations by remember { mutableStateOf(false) }

    val downloadLauncherFolderPickerLauncher = rememberDirectoryPickerLauncher(
        title = myStringResource(Res.string.download_location),
        initialDirectory = remember(text) {
            runCatching {
                File(text).canonicalPath
            }.getOrNull()
        },
        platformSettings = FileKitPlatformSettings(
            parentWindow = LocalWindow.current
        )
    ) { directory ->
        directory?.path?.let(setText)
    }

    var widthForDropDown by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current
    Box(modifier) {
        AddDownloadPageTextField(
            text,
            setText,
            myStringResource(Res.string.location),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    widthForDropDown = with(density) {
                        it.size.width.toDp()
                    }
                },
            errorText = errorText,
            end = {
                Row {
                    MyTextFieldIcon(MyIcons.folder) {
                        downloadLauncherFolderPickerLauncher.launch()
                    }
                    MyTextFieldIcon(MyIcons.down) {
                        showLastUsedLocations = !showLastUsedLocations
                    }
                }
            }
        )
        if (showLastUsedLocations) {
            ShowSuggestions(
                width = { widthForDropDown },
                suggestions = lastUsedLocations,
                onSuggestionSelected = {
                    setText(it)
                    showLastUsedLocations = false
                },
                onDismiss = {
                    showLastUsedLocations = false
                }
            )
        }
    }
}

@Composable
private fun ShowSuggestions(
    width: () -> Dp,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    MyDropDown(onDismiss) {
        Column(
            Modifier
                .width(width())
                .clip(myShapes.defaultRounded)
                .background(myColors.surface)
                .verticalScroll(rememberScrollState())
        ) {
            for (l in suggestions) {
                Text(
                    text = l,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSuggestionSelected(l)
                        }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    fontSize = myTextSizes.sm
                )
            }
        }
    }
}
