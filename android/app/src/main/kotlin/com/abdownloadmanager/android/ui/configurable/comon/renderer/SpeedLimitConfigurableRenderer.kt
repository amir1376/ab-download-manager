package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.RenderSpinner
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.configurable.item.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.DoubleTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.LocalSpeedUnit
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.datasize.SizeConverter
import ir.amirab.util.datasize.SizeFactors
import ir.amirab.util.datasize.SizeUnit
import ir.amirab.util.datasize.SizeWithUnit
import ir.amirab.util.datasize.asConverterConfig

object SpeedLimitConfigurableRenderer : ConfigurableRenderer<SpeedLimitConfigurable> {
    @Composable
    override fun RenderConfigurable(configurable: SpeedLimitConfigurable, configurableUiProps: ConfigurableUiProps) {
        RenderSpeedConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderSpeedConfig(cfg: SpeedLimitConfigurable, configurableUiProps: ConfigurableUiProps) {
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
            configurableUiProps.modifier.padding(configurableUiProps.itemPaddingValues),
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
                                .width(250.dp)
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
                            Spacer(Modifier.width(8.dp))
                            RenderSpinner(
                                possibleValues = units,
                                value = currentUnit,
                                modifier = Modifier.Companion,
                                enabled = enabled && hasLimitSpeed,
                                onSelect = {
                                    currentUnit = it
                                }
                            ) {
                                val prettified = remember(it) {
                                    "$it/s"
                                }
                                Text(prettified, Modifier.padding(horizontal = 4.dp))
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
}
