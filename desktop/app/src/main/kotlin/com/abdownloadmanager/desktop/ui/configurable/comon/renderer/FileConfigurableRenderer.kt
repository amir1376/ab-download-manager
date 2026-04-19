package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.desktop.ui.util.rememberMyFilePickerLauncher
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.FileConfigurable
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.compose.asStringSource
import java.io.File

object FileConfigurableRenderer : ConfigurableRenderer<FileConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FileConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderFileConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderFileConfig(cfg: FileConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set

        val pickFileLauncher = rememberMyFilePickerLauncher(
            title = cfg.title.rememberString(),
            initialDirectory = remember(value) {
                runCatching {
                    val file = File(value)
                    when {
                        value.isBlank() -> null
                        file.isDirectory -> file.canonicalPath
                        else -> file.parentFile?.canonicalPath
                    }
                }.getOrNull()
            },
        ) { filePath ->
            filePath?.let(setValue)
        }

        ConfigTemplate(
            modifier = configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
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
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            cfg.onPreview?.let { onPreview ->
                                TransparentIconActionButton(
                                    icon = MyIcons.speaker,
                                    contentDescription = "Preview sound".asStringSource(),
                                    modifier = Modifier.size(28.dp),
                                    iconSize = 16.dp,
                                    onClick = onPreview,
                                )
                                Spacer(Modifier.size(2.dp))
                            }
                            TransparentIconActionButton(
                                icon = MyIcons.folder,
                                contentDescription = "Choose file".asStringSource(),
                                modifier = Modifier.size(28.dp),
                                iconSize = 16.dp,
                                onClick = { pickFileLauncher.launch() },
                            )
                        }
                    }
                )
            }
        )
    }
}
