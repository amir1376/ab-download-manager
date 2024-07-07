package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.EnumConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.EnumConfigurable.RenderMode.*
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*

@Composable
fun RenderEnumConfig(cfg: EnumConfigurable<Any?>, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val index = remember(cfg.possibleValues, value) {
        cfg.possibleValues.indexOf(value)
    }
    val enabled= isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, false)
        },
        value = {
            when (cfg.renderMode) {
                Spinner -> RenderSpinner(
                    possibleValues = cfg.possibleValues,
                    value = value,
                    onSelect = {
                        setValue(it)
                    },
                    modifier = Modifier.widthIn(min = 160.dp),
                    enabled = enabled,
                    render = {
                        Text(cfg.describe(it))
                    })
            }
        }
    )
}
