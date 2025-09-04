package com.abdownloadmanager.desktop.storage

import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.ConvertSizeConfig

enum class SupportedSizeUnits {
    BinaryBits,
    BinaryBytes,
    DecimalBits,
    DecimalBytes;

    fun toConfig(): ConvertSizeConfig {
        return when (this) {
            BinaryBits -> CommonSizeConvertConfigs.BinaryBits
            BinaryBytes -> CommonSizeConvertConfigs.BinaryBytes
            DecimalBits -> CommonSizeConvertConfigs.DecimalBits
            DecimalBytes -> CommonSizeConvertConfigs.DecimalBytes
        }
    }

    companion object {
        fun fromConfig(config: ConvertSizeConfig): SupportedSizeUnits? {
            return when (config) {
                CommonSizeConvertConfigs.BinaryBits -> BinaryBits
                CommonSizeConvertConfigs.BinaryBytes -> BinaryBytes
                CommonSizeConvertConfigs.DecimalBits -> DecimalBits
                CommonSizeConvertConfigs.DecimalBytes -> DecimalBytes
                else -> null
            }
        }
    }
}
