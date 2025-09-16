package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.MyTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.utils.configurable.Configurable
import com.abdownloadmanager.shared.utils.ui.theme.myShapes
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class StringConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource),
    validate: (String) -> Boolean = { true },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<String>(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderStringConfig(this, modifier)
    }
}

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
                modifier = Modifier.fillMaxWidth(),
                text = value,
                onTextChange = {
                    setValue(it)
                },
                shape = myShapes.defaultRounded,
                textPadding = PaddingValues(4.dp),
                placeholder = "",
            )
        }
    )
}
