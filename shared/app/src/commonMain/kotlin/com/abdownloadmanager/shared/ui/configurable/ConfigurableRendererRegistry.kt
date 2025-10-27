package com.abdownloadmanager.shared.ui.configurable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

// >>>>>>>>>
// only use these in public
fun ConfigurableRendererRegistry(
    builder: ConfigurableRendererRegistryBuilder.() -> Unit
): ConfigurableRendererRegistry {
    return ConfigurableRendererRegistry(
        map = ConfigurableRendererRegistryBuilder()
            .apply(builder).map
    )
}

@Composable
fun <T> Configurable<T>.Render(
    configurableUiProps: ConfigurableUiProps,
) {
    LocalConfigurationRendererRegistry.current
        .get(this)
        .RenderConfigurable(this, configurableUiProps)
}

// <<<<<<<<
// end of public api


class ConfigurableRendererRegistryBuilder internal constructor() {
    @PublishedApi
    internal val map: MutableMap<Configurable.Key, ConfigurableRenderer<*>> = mutableMapOf()
    fun <
            TConfigurable : Configurable<*>,
            TConfigurableRenderer : ConfigurableRenderer<TConfigurable>
            > register(
        key: Configurable.Key,
        renderer: TConfigurableRenderer
    ) {
        map[key] = renderer
    }
}


class ConfigurableRendererRegistry internal constructor(
    private val map: MutableMap<Configurable.Key, ConfigurableRenderer<*>>
) {
    fun <
            TConfigurable : Configurable<*>,
            TConfigurableRenderer : ConfigurableRenderer<TConfigurable>
            > get(
        configurable: TConfigurable
    ): TConfigurableRenderer {
        val renderer = requireNotNull(map[configurable.getKey()]) {
            "renderer for $configurable not found"
        }
        @Suppress("UNCHECKED_CAST")
        return renderer as TConfigurableRenderer
    }
}

val LocalConfigurationRendererRegistry = staticCompositionLocalOf<ConfigurableRendererRegistry> {
    error("LocalConfigurationRendererRegistry not provided")
}
