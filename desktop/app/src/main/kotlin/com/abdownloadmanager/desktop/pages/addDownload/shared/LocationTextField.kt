package com.abdownloadmanager.desktop.pages.addDownload.shared

import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberDialogState
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.desktop.window.moveSafe
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.LabeledCheckbox
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import com.abdownloadmanager.desktop.ui.util.rememberMyDirectoryPickerLauncher
import com.abdownloadmanager.shared.pages.adddownload.shared.LastUsedLocationsProp
import com.abdownloadmanager.shared.pages.adddownload.shared.RememberFolderProp
import com.abdownloadmanager.shared.pages.adddownload.shared.rememberDescriptionString
import java.awt.MouseInfo
import java.io.File

@Composable
fun LocationTextField(
    modifier: Modifier,
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    lastUsedLocations: LastUsedLocationsProp,
    rememberFolderAsDefault: RememberFolderProp,
) {
    var showLocationOptions by remember { mutableStateOf(false) }

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

    Box(modifier) {
        MyTextFieldWithIcons(
            text,
            setText,
            myStringResource(Res.string.location),
            modifier = Modifier.fillMaxWidth(),
            errorText = errorText,
            end = {
                Row {
                    MyTextFieldIcon(
                        icon = MyIcons.folder,
                        contentDescription = myStringResource(Res.string.download_location),
                    ) {
                        downloadLauncherFolderPickerLauncher.launch()
                    }
                    MyTextFieldIcon(
                        icon = MyIcons.down,
                        contentDescription = myStringResource(Res.string.download_location),
                    ) {
                        showLocationOptions = !showLocationOptions
                    }
                }
            }
        )
        if (showLocationOptions) {
            LocationOptionsDialog(
                onDismiss = {
                    showLocationOptions = false
                },
                lastUsedLocations = lastUsedLocations,
                onSuggestionSelected = {
                    setText(it)
                    showLocationOptions = false
                },
                rememberFolder = rememberFolderAsDefault,
            )
        }
    }
}

@Composable
private fun LocationOptionsDialog(
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    lastUsedLocations: LastUsedLocationsProp,
    rememberFolder: RememberFolderProp,
) {
    val state = rememberDialogState(
        initialBoundsProvider = WindowBoundsProvider(
            sizeProvider = WindowSizeProvider.Unconstrained
        )
    )
    BaseOptionDialog(
        onCloseRequest = onDismiss,
        state = state,
        resizeable = false,
    ) {
        LaunchedEffect(window) {
            window.moveSafe(
                MouseInfo.getPointerInfo().location.run {
                    DpOffset(
                        x = x.dp,
                        y = y.dp
                    )
                }
            )
        }
        val shape = myShapes.defaultRounded
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 320.dp, max = 460.dp)
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            myColors.surface,
                            myColors.background,
                        )
                    )
                )
        ) {
            WithContentColor(myColors.onBackground) {
                Column {
                    WindowDraggableArea(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                myStringResource(Res.string.download_location),
                                modifier = Modifier.weight(1f),
                                fontSize = myTextSizes.lg,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.width(8.dp))
                            MyIcon(
                                MyIcons.clear,
                                null,
                                Modifier
                                    .clickable { onDismiss() }
                                    .size(16.dp)
                                    .alpha(0.5f)
                            )
                        }
                    }
                    Divider()
                    Column(
                        Modifier.padding(16.dp)
                    ) {
                        LabeledCheckbox(
                            value = rememberFolder.value,
                            onValueChange = rememberFolder.onValueChange,
                            enabled = rememberFolder.isEnabled,
                            description = rememberFolder.rememberDescriptionString(),
                        )
                    }
                    val hasLocations = lastUsedLocations.locations.isNotEmpty()
                    if (hasLocations) {
                        Divider()
                    }
                    if (hasLocations) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = myStringResource(Res.string.history),
                                fontSize = myTextSizes.base,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                for (l in lastUsedLocations.locations) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(myShapes.defaultRounded)
                                            .clickable {
                                                onSuggestionSelected(l)
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = l,
                                            modifier = Modifier.weight(1f),
                                            fontSize = myTextSizes.sm,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        MyIcon(
                                            MyIcons.clear,
                                            null,
                                            Modifier
                                                .clickable {
                                                    lastUsedLocations.onRequestRemove(l)
                                                }
                                                .padding(2.dp)
                                                .size(12.dp)
                                                .alpha(0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10)
    )
}
