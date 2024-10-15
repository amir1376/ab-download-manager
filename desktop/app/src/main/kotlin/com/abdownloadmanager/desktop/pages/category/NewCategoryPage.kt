package com.abdownloadmanager.desktop.pages.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.addDownload.single.MyTextFieldIcon
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.widget.MyIcon
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import ir.amirab.util.compose.IconSource
import ir.amirab.util.desktop.LocalWindow
import java.io.File

@Composable
fun NewCategory(
    categoryComponent: CategoryComponent,
) {
    WindowTitle(
        if (categoryComponent.isEditMode) "Edit Category"
        else "Add Category"
    )
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .padding(vertical = 16.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row {
                CategoryIcon(
                    iconSource = categoryComponent.icon.collectAsState().value,
                    onChange = categoryComponent::setIcon
                )
                Spacer(Modifier.width(16.dp))
                CategoryName(
                    modifier = Modifier.weight(1f),
                    name = categoryComponent.name.collectAsState().value,
                    onNameChanged = categoryComponent::setName
                )
            }
            Spacer(Modifier.height(12.dp))
            CategoryAutoTypes(
                types = categoryComponent.types.collectAsState().value,
                onTypesChanged = categoryComponent::setTypes
            )
            Spacer(Modifier.height(12.dp))
            CategoryDefaultPath(
                path = categoryComponent.path.collectAsState().value,
                onPathChanged = categoryComponent::setPath,
                defaultDownloadLocation = categoryComponent.defaultDownloadLocation.collectAsState().value
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)) {
            ActionButton(
                when (categoryComponent.isEditMode) {
                    true -> "Change"
                    false -> "Add"
                },
                enabled = categoryComponent.canSubmit.collectAsState().value,
                onClick = {
                    categoryComponent.submit()
                }
            )
            Spacer(Modifier.width(8.dp))
            ActionButton(
                "Cancel",
                onClick = {
                    categoryComponent.close()
                }
            )
        }
    }
}

@Composable
fun CategoryDefaultPath(
    defaultDownloadLocation: String,
    path: String,
    onPathChanged: (String) -> Unit,
) {
    val initialDirectory = remember(path, defaultDownloadLocation) {
        path
            .takeIf { it.isNotBlank() }
            ?.let {
                runCatching {
                    File(path).canonicalPath
                }.getOrNull()
            } ?: defaultDownloadLocation
    }
    val downloadFolderPickerLauncher = rememberDirectoryPickerLauncher(
        title = "Category Download Location",
        initialDirectory = initialDirectory,
        platformSettings = FileKitPlatformSettings(
            parentWindow = LocalWindow.current
        )
    ) { directory ->
        directory?.path?.let(onPathChanged)
    }

    WithLabel(
        "Category Download Location",
        helpText = """When this category chosen in "Add Download Page" use this directory as "Download Location"""
    ) {
        CategoryPageTextField(
            text = path,
            onTextChange = onPathChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "",
            errorText = null,
            end = {
                MyTextFieldIcon(MyIcons.folder) {
                    downloadFolderPickerLauncher.launch()
                }
            }
        )
    }
}

@Composable
fun CategoryAutoTypes(
    types: String,
    onTypesChanged: (String) -> Unit,
) {
    WithLabel(
        label = "Category file types",
        helpText = "Automatically put download to these file types to this category. (when you add new download)\n Separate file extensions with space (ext1 ext2 ...) "
    ) {
        CategoryPageTextField(
            text = types,
            onTextChange = onTypesChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "ext1 ext2 ext3 (separate with space)",
            singleLine = false,
            minLines = 2,
            maxLines = 2,
        )
    }
}

@Composable
fun CategoryName(
    name: String,
    onNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WithLabel(
        "Category Name",
        modifier,
    ) {
        CategoryPageTextField(
            text = name,
            onTextChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Something...",
        )
    }
}

@Composable
private fun WithLabel(
    label: String,
    modifier: Modifier = Modifier,
    helpText: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label)
            helpText?.let {
                Spacer(Modifier.width(8.dp))
                Help(helpText)
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun CategoryIcon(
    iconSource: IconSource?,
    onChange: (IconSource) -> Unit,
) {
    var showIconPicker by remember {
        mutableStateOf(false)
    }
    WithLabel(
        "Icon"
    ) {
        RenderIcon(
            icon = iconSource,
            requiresAttention = iconSource == null,
            onClick = {
                showIconPicker = !showIconPicker
            }
        )
        if (showIconPicker) {
            IconPick(
                selectedIcon = iconSource,
                icons = listOf(
                    MyIcons.pictureFile,
                    MyIcons.musicFile,
                    MyIcons.zipFile,
                    MyIcons.videoFile,
                    MyIcons.applicationFile,
                    MyIcons.documentFile,
                    MyIcons.otherFile,

                    MyIcons.file,
                    MyIcons.folder,

                    MyIcons.browserIntegration,
                    MyIcons.appearance,

                    MyIcons.settings,
                    MyIcons.search,
                    MyIcons.info,
                    MyIcons.check,
                    MyIcons.link,
                    MyIcons.download,
                    MyIcons.speaker,
                    MyIcons.group,
                    MyIcons.activeCount,
                    MyIcons.speed,
                    MyIcons.resume,
                    MyIcons.pause,
                    MyIcons.stop,
                    MyIcons.queue,
                    MyIcons.remove,
                    MyIcons.clear,
                    MyIcons.add,
                    MyIcons.paste,
                    MyIcons.copy,
                    MyIcons.refresh,
                    MyIcons.share,
                    MyIcons.lock,
                    MyIcons.question,
                    MyIcons.verticalDirection,
                    MyIcons.downloadEngine,
                    MyIcons.network,
                    MyIcons.externalLink,
                ),
                onSelected = {
                    onChange(it)
                    showIconPicker = false
                },
                onCancel = {
                    showIconPicker = false
                }
            )
        }
    }
}


@Composable
private fun RenderIcon(
    icon: IconSource?,
    indicateActive: Boolean = false,
    requiresAttention: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .border(
                1.dp,
                myColors.onBackground / 10,
                shape
            )
            .ifThen(indicateActive || requiresAttention) {
                border(
                    1.dp,
                    myColors.primary / if (indicateActive) 1f else alphaFlicker(),
                    shape
                )
            }
            .clip(shape)
            .background(myColors.surface)
            .clickable {
                onClick()
            }
            .padding(6.dp)
    ) {
        val modifier = Modifier
            .size(20.dp)
        if (icon != null) {
            MyIcon(
                icon,
                null,
                modifier,
            )
        } else {
            Spacer(modifier)
        }
    }
}


@Composable
private fun CategoryPageTextField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    errorText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    start: @Composable (() -> Unit)? = null,
    end: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val dividerModifier = Modifier
        .fillMaxHeight()
        .padding(vertical = 1.dp)
        //to not conflict with text-field border
        .width(1.dp)
        .background(if (isFocused) myColors.onBackground / 10 else Color.Transparent)
    Column(modifier) {
        MyTextField(
            text = text,
            onTextChange = onTextChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines,
            minLines = minLines,
            singleLine = singleLine,
            background = myColors.surface / 50,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(6.dp),
            start = start?.let {
                {
                    WithContentAlpha(0.5f) {
                        it()
                    }
                    Spacer(dividerModifier)
                }
            },
            end = end?.let {
                {
                    Spacer(dividerModifier)
                    it()
                }
            }
        )
        AnimatedVisibility(errorText != null) {
            if (errorText != null) {
                Text(
                    errorText,
                    Modifier.padding(bottom = 4.dp, start = 4.dp),
                    fontSize = myTextSizes.sm,
                    color = myColors.error,
                )
            }
        }
    }
}

