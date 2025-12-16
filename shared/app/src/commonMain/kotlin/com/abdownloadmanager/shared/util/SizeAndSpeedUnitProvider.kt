package com.abdownloadmanager.shared.util

import ir.amirab.util.datasize.ConvertSizeConfig
import kotlinx.coroutines.flow.StateFlow

interface SizeAndSpeedUnitProvider {
    val sizeUnit: StateFlow<ConvertSizeConfig>
    val speedUnit: StateFlow<ConvertSizeConfig>
}
