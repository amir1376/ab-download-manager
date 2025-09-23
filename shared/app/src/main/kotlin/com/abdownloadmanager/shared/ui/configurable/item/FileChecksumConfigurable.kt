package com.abdownloadmanager.shared.ui.configurable.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.utils.FileChecksum
import com.abdownloadmanager.shared.utils.FileChecksumAlgorithm
import com.abdownloadmanager.shared.ui.configurable.ConfigTemplate
import com.abdownloadmanager.shared.ui.configurable.RenderSpinner
import com.abdownloadmanager.shared.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.resources.myStringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileChecksumConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<FileChecksum?>,
    describe: (FileChecksum?) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<FileChecksum?>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderFileChecksumConfig(this, modifier)
    }
}

@Composable
private fun RenderFileChecksumConfig(cfg: FileChecksumConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set

    val enabled = isConfigEnabled()
    val hasFileChecksum = value != null
    ConfigTemplate(
        modifier,
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
                                modifier = Modifier,
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
