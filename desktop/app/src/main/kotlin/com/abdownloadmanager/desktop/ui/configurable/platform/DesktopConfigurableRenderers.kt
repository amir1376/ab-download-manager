package com.abdownloadmanager.desktop.ui.configurable.platform

import com.abdownloadmanager.desktop.ui.configurable.platform.item.FontConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.desktop.ui.configurable.platform.renderer.FontConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.ProxyConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ContainsConfigurableRenderers

data class DesktopConfigurableRenderers(
    val fontConfigurableRenderer: ConfigurableRenderer<FontConfigurable>,
) : ContainsConfigurableRenderers {
    override fun getAllRenderers(): Map<Configurable.Key, ConfigurableRenderer<*>> {
        return mapOf(
            FontConfigurable.Key to fontConfigurableRenderer,
        )
    }
}

val PlatformConfigurableRenderersForDesktop = DesktopConfigurableRenderers(
    fontConfigurableRenderer = FontConfigurableRenderer,
)
