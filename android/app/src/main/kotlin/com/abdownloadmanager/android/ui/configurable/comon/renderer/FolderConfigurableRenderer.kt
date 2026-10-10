package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.abdownloadmanager.android.pages.directorypicker.rememberAndroidDirectoryPickerLauncher
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.FolderConfigurable
import java.io.File

object FolderConfigurableRenderer : ConfigurableRenderer<FolderConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FolderConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderFolderConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderFolderConfig(cfg: FolderConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set

        val pickFolderLauncher = rememberAndroidDirectoryPickerLauncher(
            title = cfg.title,
            initialDirectory = remember(value) {
                runCatching {
                    File(value).canonicalPath
                }.getOrNull()
            },
        ) { directory ->
            directory?.let(setValue)
        }

        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable { pickFolderLauncher.launch() }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg)
            },
            value = {
                NextIcon()
            }
        )
    }
}
