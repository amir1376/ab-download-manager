package com.abdownloadmanager.shared.util

import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.ConvertSizeConfig
import ir.amirab.util.datasize.SizeWithUnit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.datasize.*
import ir.amirab.util.datasize.SizeWithUnit.Companion.DefaultFormat
import ir.amirab.util.datasize.SizeWithUnit.Companion.SmallFormat
import java.text.NumberFormat

val LocalSpeedUnit = compositionLocalOf {
    CommonSizeConvertConfigs.BinaryBytes
}
val LocalSizeUnit = compositionLocalOf {
    CommonSizeConvertConfigs.BinaryBytes
}

@Composable
fun ProvideSizeAndSpeedUnit(
    sizeUnitConfig: ConvertSizeConfig,
    speedUnitConfig: ConvertSizeConfig,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpeedUnit provides speedUnitConfig,
        LocalSizeUnit provides sizeUnitConfig,
        content = content
    )
}


// they are used for ui
// size == -1 means that its unknown

fun convertPositiveBytesToSizeUnit(
    size: Long,
    target: ConvertSizeConfig,
): SizeWithUnit? {
    if (size < 0) return null
    return SizeConverter.bytesToSize(
        bytes = size,
        target = target,
    )
}

fun convertPositiveBytesToHumanReadable(
    size: Long,
    target: ConvertSizeConfig,
    asCompactAsPossible: Boolean = false,
): String? {
    val format = if (asCompactAsPossible) SmallFormat else DefaultFormat
    return convertPositiveBytesToSizeUnit(size, target)
        ?.let {
            buildString {
                append(it.formatedValue(format))
                if (!asCompactAsPossible) {
                    append(" ")
                }
                append(it.unit.toString())
            }
        }
}

fun convertPositiveSizeToHumanReadable(
    size: Long,
    target: ConvertSizeConfig,
    asCompactAsPossible: Boolean = false,
): StringSource {
    return convertPositiveBytesToHumanReadable(size, target, asCompactAsPossible)
        ?.asStringSource()
        ?: Res.string.unknown.asStringSource()
}

fun convertPositiveSpeedToHumanReadable(size: Long, target: ConvertSizeConfig, perUnit: String = "s"): String {
    return convertPositiveBytesToHumanReadable(size, target)
        ?.let { "$it/$perUnit" }
        ?: "-"
}
