package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.FolderConfigurable
import com.abdownloadmanager.desktop.ui.icon.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.widget.MyTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import ir.amirab.util.desktop.LocalWindow
import java.io.File

@Composable
fun RenderFolderConfig(cfg: FolderConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set

    val pickFolderLauncher = rememberDirectoryPickerLauncher(
        title = cfg.title,
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
                shape = RectangleShape,
                textPadding = PaddingValues(4.dp),
                placeholder = cfg.title,
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