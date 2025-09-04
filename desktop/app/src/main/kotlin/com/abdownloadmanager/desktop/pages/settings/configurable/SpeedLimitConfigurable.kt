package com.abdownloadmanager.desktop.pages.settings.configurable

import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.DoubleTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.utils.configurable.BaseLongConfigurable
import com.abdownloadmanager.shared.utils.LocalSpeedUnit
import ir.amirab.util.compose.StringSource
import ir.amirab.util.datasize.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeedLimitConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<Long>,
    describe: (Long) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : BaseLongConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    range = 0..Long.MAX_VALUE,
    enabled = enabled,
    visible = visible,
) {
    @Composable
    override fun render(modifier: Modifier) {
        RenderSpeedConfig(this, modifier)
    }
}

@Composable
private fun RenderSpeedConfig(cfg: SpeedLimitConfigurable, modifier: Modifier) {
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
                                    256.0, currentUnit
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
