package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.desktop.ui.util.rememberMyFilePickerLauncher
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.SoundConfigurable
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.util.notification.INotificationSound
import com.abdownloadmanager.shared.util.notification.platformNotificationSound
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import io.github.vinceglb.filekit.core.PickerType
import java.io.File

object SoundConfigurableRenderer : ConfigurableRenderer<SoundConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: SoundConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderSoundConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderSoundConfig(cfg: SoundConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set

        val pickFileLauncher = rememberMyFilePickerLauncher(
            title = cfg.title.rememberString(),
            initialDirectory = remember(value) {
                if (value == INotificationSound.DEFAULT_VALUE) {
                    null
                } else {
                    runCatching {
                        File(value).parentFile.canonicalPath
                    }.getOrNull()
                }
            },
            fileTypes = PickerType.File(listOf("wav")),
            onResult = { file ->
                file?.let(setValue)
            }
        )


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
                        if (value != INotificationSound.DEFAULT_VALUE) {
                            MyIcon(
                                icon = MyIcons.clear,
                                contentDescription = null,
                                modifier = Modifier
                                    .pointerHoverIcon(PointerIcon.Default)
                                    .fillMaxHeight()
                                    .clickable { setValue(INotificationSound.DEFAULT_VALUE) }
                                    .wrapContentHeight()
                                    .padding(horizontal = 8.dp)
                                    .size(16.dp)
                            )
                        }
                        MyIcon(
                            icon = MyIcons.resume,
                            contentDescription = null,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Default)
                                .fillMaxHeight()
                                .clickable {
                                    platformNotificationSound()
                                        .actualPlay(value)
                                }
                                .wrapContentHeight()
                                .padding(horizontal = 8.dp)
                                .size(16.dp)
                        )
                        MyIcon(
                            icon = MyIcons.folder,
                            contentDescription = null,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Default)
                                .fillMaxHeight()
                                .clickable { pickFileLauncher.launch() }
                                .wrapContentHeight()
                                .padding(horizontal = 8.dp)
                                .size(16.dp)
                        )
                    }
                )
            }
        )
    }
}
