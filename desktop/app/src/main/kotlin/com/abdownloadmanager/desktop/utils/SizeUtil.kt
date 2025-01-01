package com.abdownloadmanager.desktop.utils

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

fun convertPositiveBytesToHumanReadable(size: Long, target: ConvertSizeConfig): String? {
    return convertPositiveBytesToSizeUnit(size, target)
        ?.let { "${it.formatedValue()} ${it.unit}" }
}

fun convertPositiveSizeToHumanReadable(size: Long, target: ConvertSizeConfig): StringSource {
    return convertPositiveBytesToHumanReadable(size, target)
        ?.asStringSource()
        ?: Res.string.unknown.asStringSource()
}

fun convertPositiveSpeedToHumanReadable(size: Long, target: ConvertSizeConfig, perUnit: String = "s"): String {
    return convertPositiveBytesToHumanReadable(size, target)
        ?.let { "$it/$perUnit" }
        ?: "-"
}
