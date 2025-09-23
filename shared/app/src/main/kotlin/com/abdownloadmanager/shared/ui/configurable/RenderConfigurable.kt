package com.abdownloadmanager.shared.ui.configurable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class ConfigGroupInfo(
    val visible: Boolean,
    val enabled: Boolean,
)

@Suppress("UNCHECKED_CAST")
@Composable
fun RenderConfigurable(
    cfg: Configurable<*>,
    modifier: Modifier,
    groupInfo: ConfigGroupInfo? = null,
) {
    ConfigurationWrapper(
        configurable = cfg,
        groupInfo = groupInfo
    ) {
        cfg.render(modifier)
    }
}

