package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.DoubleTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.LocalSpeedUnit
import ir.amirab.util.datasize.*

@Composable
fun RenderSpeedConfig(cfg: SpeedLimitConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set

    val speedUnit = LocalSpeedUnit.current
    val allowedFactors = listOf(
        SizeFactors.FactorValue.Kilo,
        SizeFactors.FactorValue.Mega,
    )
    val units = allowedFactors.map {
        SizeUnit(
            factorValue = it,
            baseSize = speedUnit.baseSize,
            factors = speedUnit.factors
        )
    }
    val enabled = isConfigEnabled()
    val hasLimitSpeed = value > 0L

    var currentUnit by remember(hasLimitSpeed) {
        mutableStateOf(
            SizeConverter.bytesToSize(
                value,
                speedUnit.copy(acceptedFactors = allowedFactors)
            ).unit
        )
    }
    var currentValue by remember(value) {
        val v = SizeConverter.bytesToSize(
            value, currentUnit.asConverterConfig()
        ).formatedValue().toDouble()
        mutableStateOf(v)
    }
    LaunchedEffect(currentValue, currentUnit) {
        setValue(
            SizeConverter.sizeToBytes(
                SizeWithUnit(currentValue, currentUnit),
            )
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
                                "$it/s"
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
                        setValue(
                            SizeConverter.sizeToBytes(
                                SizeWithUnit(
                                    256.0, SizeUnit(
                                        SizeFactors.FactorValue.Kilo,
                                        BaseSize.Bytes,
                                        SizeFactors.BinarySizeFactors,
                                    )
                                )
                            )
                        )
                    } else {
                        setValue(0)
                    }
                })
        }
    )
}