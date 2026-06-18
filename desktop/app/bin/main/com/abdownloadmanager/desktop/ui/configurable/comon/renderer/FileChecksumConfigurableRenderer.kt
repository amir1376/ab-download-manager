package com.abdownloadmanager.desktop.ui.configurable.comon.renderer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.desktop.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.RenderSpinner
import com.abdownloadmanager.desktop.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.FileChecksumConfigurable
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.FileChecksum
import com.abdownloadmanager.shared.util.FileChecksumAlgorithm
import ir.amirab.util.compose.resources.myStringResource

object FileChecksumConfigurableRenderer : ConfigurableRenderer<FileChecksumConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: FileChecksumConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderFileChecksumConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderFileChecksumConfig(cfg: FileChecksumConfigurable, configurableUiProps: ConfigurableUiProps) {
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set

        val enabled = isConfigEnabled()
        val hasFileChecksum = value != null
        ConfigTemplate(
            configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitleAndDescription(cfg, true)
                }
            },
            nestedContent = {
                Column(Modifier.align(Alignment.End)) {
                    AnimatedVisibility(
                        hasFileChecksum,
                    ) {
                        value?.let { value ->
                            Row(
                                Modifier
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RenderSpinner(
                                    possibleValues = FileChecksumAlgorithm
                                        .all()
                                        .map { it.algorithm },
                                    value = value.algorithm,
                                    modifier = Modifier.Companion,
                                    enabled = enabled,
                                    onSelect = {
                                        setValue(value.copy(algorithm = it))
                                    }
                                ) {
                                    Text(it)
                                }
                                Text(":", Modifier.padding(horizontal = 4.dp))
                                MyTextField(
                                    text = value.value,
                                    onTextChange = {
                                        setValue(value.copy(value = it))
                                    },
                                    shape = RectangleShape,
                                    textPadding = PaddingValues(4.dp),
                                    enabled = enabled,
                                    modifier = Modifier.weight(1f),
                                    placeholder = myStringResource(Res.string.file_checksum),
                                )
                            }
                        }
                    }
                }
            },
            value = {
                CheckBox(
                    value = hasFileChecksum,
                    enabled = enabled,
                    onValueChange = {
                        if (it) {
                            setValue(
                                FileChecksum(
                                    FileChecksumAlgorithm.default().algorithm,
                                    "",
                                )
                            )
                        } else {
                            setValue(null)
                        }
                    })
            }
        )
    }
}
