package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.settings.FontInfo
import com.abdownloadmanager.desktop.utils.configurable.BaseEnumConfigurable
import ir.amirab.util.compose.StringSource
import ir.amirab.util.ifThen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FontConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<FontInfo>,
    describe: (FontInfo) -> StringSource,
    possibleValues: List<FontInfo>,
    valueToString: (FontInfo) -> List<String> = {
        listOf(it.name.getString())
    },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseEnumConfigurable<FontInfo>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    possibleValues = possibleValues,
    valueToString = valueToString,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderFontConfig(this, modifier)
    }
}


@Composable
private fun RenderFontConfig(cfg: FontConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val enabled = isConfigEnabled()
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
                valueToString = cfg.valueToString,
                modifier = Modifier.widthIn(min = 160.dp),
                enabled = enabled,
                render = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.ifThen(!enabled) {
                            alpha(0.5f)
                        }
                    ) {
                        Text(
                            cfg.describe(it).rememberString(),
                            fontFamily = it.fontFamily,
                            fontSize = myTextSizes.lg,
                        )
                    }
                })
        }
    )
}
