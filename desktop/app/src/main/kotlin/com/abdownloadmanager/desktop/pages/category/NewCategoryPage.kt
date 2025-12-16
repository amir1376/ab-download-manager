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
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.category.CategoryComponent
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.resources.myStringResource
import com.abdownloadmanager.shared.ui.util.rememberMyDirectoryPickerLauncher
import java.io.File

@Composable
fun NewCategory(
    categoryComponent: CategoryComponent,
) {
    WindowTitle(
        myStringResource(
            if (categoryComponent.isEditMode) {
                Res.string.edit_category
            } else {
                Res.string.add_category
            }
        )
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
                onTypesChanged = categoryComponent::setTypes,
                enabled = categoryComponent.typesEnabled.collectAsState().value,
                setEnabled = categoryComponent::setTypesEnabled
            )
            Spacer(Modifier.height(12.dp))
            CategoryAutoUrls(
                urlPatterns = categoryComponent.urlPatterns.collectAsState().value,
                onUrlPatternChanged = categoryComponent::setUrlPatterns,
                enabled = categoryComponent.urlPatternsEnabled.collectAsState().value,
                setEnabled = categoryComponent::setUrlPatternsEnabled
            )
            Spacer(Modifier.height(12.dp))
            CategoryDefaultPath(
                path = categoryComponent.path.collectAsState().value,
                onPathChanged = categoryComponent::setPath,
                defaultDownloadLocation = categoryComponent.defaultDownloadLocation.collectAsState().value,
                checked = categoryComponent.usePath.collectAsState().value,
                setChecked = categoryComponent::setUsePath
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)) {
            ActionButton(
                myStringResource(
                    when (categoryComponent.isEditMode) {
                        true -> Res.string.change
                        false -> Res.string.add
                    }
                ),
                enabled = categoryComponent.canSubmit.collectAsState().value,
                onClick = {
                    categoryComponent.submit()
                }
            )
            Spacer(Modifier.width(8.dp))
            ActionButton(
                myStringResource(Res.string.cancel),
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
    checked: Boolean,
    setChecked: (Boolean) -> Unit,
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
    val downloadFolderPickerLauncher = rememberMyDirectoryPickerLauncher(
        title = myStringResource(Res.string.category_download_location),
        initialDirectory = initialDirectory,
        attachToWindow = true
    ) { directory ->
        directory?.let(onPathChanged)
    }

    OptionalWithLabel(
        label = myStringResource(Res.string.category_download_location),
        helpText = myStringResource(Res.string.category_download_location_description),
        enabled = checked,
        setEnabled = setChecked,
    ) {
        CategoryPageTextField(
            text = path,
            onTextChange = onPathChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = checked,
            placeholder = "",
            errorText = null,
            end = {
                MyTextFieldIcon(
                    MyIcons.folder,
                    enabled = checked,
                ) {
                    downloadFolderPickerLauncher.launch()
                }
            }
        )
    }
}

@Composable
fun CategoryAutoTypes(
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit,
    types: String,
    onTypesChanged: (String) -> Unit,
) {
    OptionalWithLabel(
        label = myStringResource(Res.string.category_file_types),
        helpText = myStringResource(Res.string.category_file_types_description),
        enabled = enabled,
        setEnabled = setEnabled,
    ) {
        CategoryPageTextField(
            text = types,
            onTextChange = onTypesChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "ext1 ext2 ext3",
            enabled = enabled,
            singleLine = false,
        )
    }
}

@Composable
fun CategoryAutoUrls(
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit,
    urlPatterns: String,
    onUrlPatternChanged: (String) -> Unit,
) {
    OptionalWithLabel(
        label = myStringResource(Res.string.category_url_patterns),
        helpText = myStringResource(Res.string.category_url_patterns_description),
        enabled = enabled,
        setEnabled = setEnabled
    ) {
        CategoryPageTextField(
            text = urlPatterns,
            onTextChange = onUrlPatternChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "dl.example.com/pics example.com/*/path",
            enabled = enabled,
            singleLine = false,
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
        myStringResource(Res.string.category_name),
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
private fun OptionalWithLabel(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit,
    helpText: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.onClick {
                    setEnabled(!enabled)
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckBox(enabled, setEnabled, size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
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
        myStringResource(Res.string.icon)
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
    enabled: Boolean = true,
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
            shape = myShapes.defaultRounded,
            enabled = enabled,
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

