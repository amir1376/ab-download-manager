package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.shared.ui.widget.MyTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun RenderStringConfig(cfg: StringConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    ConfigTemplate(
        modifier=modifier,
        title = {
            TitleAndDescription(cfg,true)
        },
        value = {
            MyTextField(
                modifier = Modifier.width(100.dp),
                text = value,
                onTextChange = {
                    setValue(it)
                },
                shape = RectangleShape,
                textPadding = PaddingValues(4.dp),
                placeholder = "",
            )
        }
    )
}