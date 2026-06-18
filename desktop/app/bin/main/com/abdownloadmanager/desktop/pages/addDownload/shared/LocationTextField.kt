package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.menu.custom.MyDropDown
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import com.abdownloadmanager.desktop.ui.util.rememberMyDirectoryPickerLauncher
import java.io.File

@Composable
fun LocationTextField(
    modifier: Modifier,
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    lastUsedLocations: List<String> = emptyList(),
    onRequestRemoveSaveLocation: (String) -> Unit,
) {
    var showLastUsedLocations by remember { mutableStateOf(false) }

    val downloadLauncherFolderPickerLauncher = rememberMyDirectoryPickerLauncher(
        title = myStringResource(Res.string.download_location),
        initialDirectory = remember(text) {
            runCatching {
                File(text).canonicalPath
            }.getOrNull()
        },
        attachToWindow = true
    ) { directory ->
        directory?.let(setText)
    }

    var widthForDropDown by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current
    Box(modifier) {
        MyTextFieldWithIcons(
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
                },
                onRequestRemove = onRequestRemoveSaveLocation
            )
        }
    }
}

@Composable
private fun ShowSuggestions(
    width: () -> Dp,
    suggestions: List<String>,
    onRequestRemove: (String) -> Unit,
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
                Row(
                    Modifier.height(IntrinsicSize.Max)
                ) {
                    Text(
                        text = l,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onSuggestionSelected(l)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        fontSize = myTextSizes.sm
                    )
                    MyIcon(
                        MyIcons.clear,
                        null,
                        Modifier
                            .fillMaxHeight()
                            .clickable {
                                onRequestRemove(l)
                            }
                            .wrapContentHeight()
                            .padding(horizontal = 2.dp)
                            .size(12.dp)
                            .alpha(0.25f)
                    )
                }
            }
        }
    }
}
