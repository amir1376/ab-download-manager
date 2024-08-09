package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.ThemeConfigurable
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun RenderThemeConfig(cfg: ThemeConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled= isConfigEnabled()
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            RenderSpinner(
                possibleValues = cfg.possibleValues, value = value, onSelect = {
                    setValue(it)
                },
                modifier = Modifier.widthIn(min = 160.dp),
                enabled = enabled,
                render = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(
                            Modifier
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(myColors.primaryGradientColors),
                                    CircleShape
                                )
                                .padding(1.dp)
                                .background(
                                    it.color,
                                )
                                .size(16.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(cfg.describe(it), fontSize = myTextSizes.lg)
                    }
                })
        }
    )
}