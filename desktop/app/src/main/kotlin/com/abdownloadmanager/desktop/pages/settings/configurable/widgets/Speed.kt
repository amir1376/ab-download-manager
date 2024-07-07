package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.ui.widget.CheckBox
import com.abdownloadmanager.desktop.ui.widget.DoubleTextField
import com.abdownloadmanager.desktop.utils.baseConvertBytesToHumanReadable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.amirab.downloader.utils.ByteConverter
import ir.amirab.downloader.utils.ByteConverter.BYTES
import ir.amirab.downloader.utils.ByteConverter.K_BYTES
import ir.amirab.downloader.utils.ByteConverter.M_BYTES

@Composable
fun RenderSpeedConfig(cfg: SpeedLimitConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val units = listOf(
        BYTES,
        K_BYTES,
        M_BYTES,
    )
    val enabled= isConfigEnabled()
    val hasLimitSpeed = value != 0L
    
    var currentUnit by remember(hasLimitSpeed) { mutableStateOf(baseConvertBytesToHumanReadable(value)?.unit ?: BYTES) }
    var currentValue by remember(value) {
        val v = ByteConverter.run {
            prettify(
                byteTo(value, currentUnit)
            ).toDouble()
        }
        mutableStateOf(v)
    }
    LaunchedEffect(currentValue, currentUnit) {
        setValue(
            ByteConverter.unitToByte(currentValue, currentUnit)
        )
    }
    ConfigTemplate(
        modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TitleAndDescription(cfg, true)
            }
        },
        nestedContent = {
            Column(Modifier.align(Alignment.End)) {
                AnimatedVisibility(hasLimitSpeed) {
                    Row(
                        Modifier
                            .padding(vertical = 8.dp)
                            .width(200.dp)
                    ) {
                        DoubleTextField(
                            value = currentValue,
                            onValueChange = {
                                currentValue = it
                            },
                            enabled = enabled && hasLimitSpeed,
                            range = 0.0..1_000.0,
                            unit = 1.0,
                            modifier = Modifier.weight(1f),
                        )
                        RenderSpinner(
                            possibleValues = units,
                            value = currentUnit,
                            modifier = Modifier,
                            enabled = enabled && hasLimitSpeed,
                            onSelect = {
                                currentUnit = it
                            }
                        ) {
                            val prettified = remember(it) {
                                ByteConverter.unitPrettify(it) + "/s"
                            }
                            Text(prettified)
                        }
                    }
                }
            }
        },
        value = {
            CheckBox(
                value = hasLimitSpeed,
                enabled = enabled,
                onValueChange = {
                if (it) {
                    setValue(ByteConverter.unitToByte(10.0, K_BYTES))
                } else {
                    setValue(0)
                }
            })
        }
    )
}