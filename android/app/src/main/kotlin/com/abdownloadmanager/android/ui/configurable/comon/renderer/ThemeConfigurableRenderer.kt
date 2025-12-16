package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.RenderSpinnerInSheet
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.ThemeConfigurable
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.ifThen

object ThemeConfigurableRenderer : ConfigurableRenderer<ThemeConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: ThemeConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderThemeConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderThemeConfig(cfg: ThemeConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set
        val enabled = isConfigEnabled()
        var isOpened by remember { mutableStateOf(false) }
        val onDismiss = {
            isOpened = false
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    isOpened = true
                }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(cfg, true)
            },
            value = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.ifThen(!enabled) {
                        alpha(0.5f)
                    }
                ) {
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
                                value.color,
                            )
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    NextIcon()
                }
            }
        )
        RenderSpinnerInSheet(
            title = cfg.title,
            onDismiss = onDismiss,
            isOpened = isOpened,
            possibleValues = cfg.possibleValues,
            value = value,
            onSelect = {
                setValue(it)
            },
            valueToString = cfg.valueToString,
            render = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.ifThen(!enabled) {
                        alpha(0.5f)
                    }
                ) {
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
                    Text(cfg.describe(it).rememberString())
                }
            })
    }

}
