package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.FileConfigurable

object FileConfigurableRenderer : ConfigurableRenderer<FileConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FileConfigurable, configurableUiProps: ConfigurableUiProps) {
        StringConfigurableRenderer.RenderStringConfig(configurable, configurableUiProps)
    }
}
