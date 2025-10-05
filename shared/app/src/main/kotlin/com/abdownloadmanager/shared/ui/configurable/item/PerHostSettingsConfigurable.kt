package com.abdownloadmanager.shared.ui.configurable.item

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.TitleAndDescription
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerHostSettingsConfigurable(
    title: StringSource,
    description: StringSource,
    val onRequestOpenPerHostSettingsWindow: () -> Unit,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<Unit>(
    title = title,
    description = description,
    backedBy = MutableStateFlow(Unit),
    describe = { "".asStringSource() },
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderPerHostSettingsConfigurable(
            cfg = this,
            modifier = modifier,
            onRequestOpenConfigWindow = onRequestOpenPerHostSettingsWindow,
        )
    }
}


@Composable
private fun RenderPerHostSettingsConfigurable(
    cfg: PerHostSettingsConfigurable,
    modifier: Modifier,
    onRequestOpenConfigWindow: () -> Unit
) {
//    val value by cfg.stateFlow.collectAsState()
//    val setValue = cfg::set
//    val enabled = isConfigEnabled()

    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            ActionButton(
                myStringResource(Res.string.change),
                onClick = onRequestOpenConfigWindow,
            )
        },
    )
}
