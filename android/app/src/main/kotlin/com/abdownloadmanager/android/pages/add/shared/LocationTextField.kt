package com.abdownloadmanager.android.pages.add.shared

import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.directorypicker.rememberAndroidDirectoryPickerLauncher
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.LabeledCheckbox
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.asStringSource
import com.abdownloadmanager.shared.pages.adddownload.shared.LastUsedLocationsProp
import com.abdownloadmanager.shared.pages.adddownload.shared.RememberFolderProp
import com.abdownloadmanager.shared.pages.adddownload.shared.rememberDescriptionString
import java.io.File

@Composable
fun LocationTextField(
    modifier: Modifier,
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    lastUsedLocations: LastUsedLocationsProp,
    rememberFolder: RememberFolderProp,
) {
    var showLocationOptions by remember { mutableStateOf(false) }

    val downloadLauncherFolderPickerLauncher = rememberAndroidDirectoryPickerLauncher(
        title = Res.string.download_location.asStringSource(),
        initialDirectory = remember(text) {
            runCatching {
                File(text).canonicalPath
            }.getOrNull()
        },
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
        LocationOptionsDialog(
            isOpen = showLocationOptions,
            onDismiss = {
                showLocationOptions = false
            },
            lastUsedLocations = lastUsedLocations,
            onSuggestionSelected = {
                setText(it)
            },
            rememberFolder = rememberFolder,
        )
    }
}

@Composable
private fun LocationOptionsDialog(
    isOpen: Boolean,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    lastUsedLocations: LastUsedLocationsProp,
    rememberFolder: RememberFolderProp,
) {
    val dialogState = rememberResponsiveDialogState(false)
    LaunchedEffect(isOpen) {
        if (isOpen) {
            dialogState.show()
        } else {
            dialogState.hide()
        }
    }
    dialogState.OnFullyDismissed {
        onDismiss()
    }
    ResponsiveDialog(
        state = dialogState,
        onDismiss = {
            dialogState.hide()
        }
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(myStringResource(Res.string.download_location))
                    },
                    headerActions = {
                        TransparentIconActionButton(
                            icon = MyIcons.close,
                            contentDescription = Res.string.close.asStringSource(),
                            onClick = { dialogState.hide() }
                        )
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            for (l in lastUsedLocations.locations) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSuggestionSelected(l)
                                            dialogState.hide()
                                        }
                                        .padding(vertical = 10.dp),
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
                                            .padding(4.dp)
                                            .size(16.dp)
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

@Composable
private fun Divider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10)
    )
}
