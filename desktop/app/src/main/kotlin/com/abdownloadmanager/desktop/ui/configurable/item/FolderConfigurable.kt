package com.abdownloadmanager.desktop.ui.configurable.item

import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.MyTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.utils.ui.theme.myShapes
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import ir.amirab.util.compose.StringSource
import ir.amirab.util.desktop.LocalWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FolderConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource),
    validate: (String) -> Boolean,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : StringConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderFolderConfig(this, modifier)
    }
}


@Composable
private fun RenderFolderConfig(cfg: FolderConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set

    val pickFolderLauncher = rememberDirectoryPickerLauncher(
        title = cfg.title.rememberString(),
        initialDirectory = remember(value) {
            runCatching {
                File(value).canonicalPath
            }.getOrNull()
        },
        platformSettings = FileKitPlatformSettings(
            parentWindow = LocalWindow.current
        )
    ) { directory ->
        directory?.path?.let(setValue)
    }


    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            MyTextField(
                modifier = Modifier.fillMaxWidth(),
                text = value,
                onTextChange = {
                    setValue(it)
                },
                shape = myShapes.defaultRounded,
                textPadding = PaddingValues(4.dp),
                placeholder = cfg.title.rememberString(),
                end = {
                    MyIcon(
                        icon = MyIcons.folder,
                        contentDescription = null,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Default)
                            .fillMaxHeight()
                            .clickable { pickFolderLauncher.launch() }
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp)
                            .size(16.dp))
                }
            )
        }
    )
}

